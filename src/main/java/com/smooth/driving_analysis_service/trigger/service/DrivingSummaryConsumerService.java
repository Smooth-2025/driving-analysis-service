package com.smooth.driving_analysis_service.trigger.service;

import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneItem;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneItemRepository;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import com.smooth.driving_analysis_service.global.redis.RedisKeys;
import com.smooth.driving_analysis_service.trigger.dto.DrivingSummaryV1;
import com.smooth.driving_analysis_service.trigger.dto.ReportTriggerV1;
import com.smooth.driving_analysis_service.trigger.producer.ReportTriggerProducer;
import com.smooth.driving_analysis_service.pipeline.RealtimeDrivingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrivingSummaryConsumerService {

    private final RedisTemplate<String, String> redis;
    private final MilestoneReportRepository reportRepo;
    private final MilestoneItemRepository itemRepo;
    private final ReportTriggerProducer producer;
    private final RealtimeDrivingService pipelineService;

    @Value("${progress.threshold:15}")
    private int threshold;

    @Transactional
    public void processDrivingSummary(String messageId, DrivingSummaryV1 s) {
        if (!s.isCompleted()) return;

        // 1) 멱등성(트립단위)
        String idemKey = RedisKeys.processedTrip(s.getDrivingId());
        Boolean first = redis.opsForValue().setIfAbsent(idemKey, "1", Duration.ofDays(7));
        if (Boolean.FALSE.equals(first)) {
            log.debug("skip duplicate trip {}", s.getDrivingId());
            return;
        }

        // 2) Pipeline 통합 통계 저장 (XADD + DrivingRecord → driving_accumulated_stats)
        pipelineService.applySummary(s);
        log.debug("Pipeline processing completed for drivingId={}", s.getDrivingId());

        Long userId = Long.valueOf(s.getUserId());

        // 2) 사용자별 ACTIVE REPORT 확보 (COLLECTING 재사용)
        MilestoneReport report = findOrCreateActiveReport(userId);

        // 3) 아이템 추가 (이미 있으면 스킵)
        if (!itemRepo.existsByReportIdAndDrivingId(report.getId(), s.getDrivingId())) {
            int nextOrder = itemRepo.countByReportId(report.getId()) + 1;

            MilestoneItem item = MilestoneItem.builder()
                    .report(report)
                    .drivingId(s.getDrivingId())
                    .orderNo(nextOrder)
                    .build();
            itemRepo.save(item);

            // 헤더의 누적 트립 수도 갱신(선택)
            report.setNumberOfDriving(nextOrder);
            reportRepo.save(report);

            log.info("Item appended reportId={}, orderNo={}, drivingId={}",
                    report.getId(), nextOrder, s.getDrivingId());
        }

        // 4) 마일스톤 도달 시 트리거 발행 (4/8/12/15회)
        int count = itemRepo.countByReportId(report.getId());
        boolean shouldTrigger = (count == 4 || count == 8 || count == 12 || count == 15);
        
        if (shouldTrigger && report.getStatus() == MilestoneReport.Status.COLLECTING) {
            
            // 15회일 때만 상태 변경 및 캐시 삭제
            if (count == 15) {
                report.setStatus(MilestoneReport.Status.PROCESSING);
                report.setNumberOfDriving(count);
                reportRepo.save(report);
                
                // active-report 캐시 제거 (다음 트립부터는 새 사이클로)
                redis.delete(RedisKeys.activeReportForUser(String.valueOf(userId)));
            }

            // 트리거 발행
            List<String> tripIds = itemRepo.findByReportIdOrderByOrderNoAsc(report.getId())
                    .stream()
                    .map(MilestoneItem::getDrivingId)
                    .toList();

            // type 결정: 15회면 FINAL, 4/8/12회면 INTERIM
            String triggerType = (count == 15) ? "FINAL" : "INTERIM";
            
            ReportTriggerV1 trigger = ReportTriggerV1.builder()
                    .v(1)
                    .type(triggerType)
                    .userId(String.valueOf(userId))
                    .reportId(report.getId())
                    .milestone(count)
                    .drivingIds(tripIds)
                    .status(count == 15 ? "PROCESSING" : "COLLECTING")
                    .emittedAt(LocalDateTime.now())
                    .producer("driving-analysis-service")
                    .traceId(UUID.randomUUID().toString())
                    .build();

            producer.emit(trigger);
            log.info("Milestone trigger emitted: type={}, reportId={}, tripCount={}", triggerType, report.getId(), count);
        }
    }

    private MilestoneReport findOrCreateActiveReport(Long userId) {
        // 캐시 우선
        String cacheKey = RedisKeys.activeReportForUser(String.valueOf(userId));
        String cached = redis.opsForValue().get(cacheKey);
        if (cached != null) {
            Long reportId = Long.valueOf(cached);
            return reportRepo.findById(reportId)
                    .orElseGet(() -> createAndCache(userId, cacheKey));
        }

        // DB 조회 (COLLECTING 재사용)
        return reportRepo.findFirstByUserIdAndStatusOrderByIdDesc(userId, MilestoneReport.Status.COLLECTING)
                .orElseGet(() -> createAndCache(userId, cacheKey));
    }

    private MilestoneReport createAndCache(Long userId, String cacheKey) {
        // 다음 cycleNo = 최신 cycleNo + 1 (없으면 1)
        int nextCycle = reportRepo.findTopByUserIdOrderByCycleNoDesc(userId)
                .map(r -> (r.getCycleNo() == null ? 0 : r.getCycleNo()) + 1)
                .orElse(1);
        MilestoneReport r = MilestoneReport.builder()
                .userId(userId)
                .cycleNo(nextCycle)
                .numberOfDriving(0)
                .status(MilestoneReport.Status.COLLECTING)
                .read(false)
                .build();

        r = reportRepo.save(r);

        // 캐시 30일
        redis.opsForValue().set(cacheKey, String.valueOf(r.getId()), Duration.ofDays(30));

        log.info("New COLLECTING report created userId={}, reportId={}, cycleNo={}", userId, r.getId(), nextCycle);
        return r;
    }

    public void handle(DrivingSummaryV1 dto) {
        // messageId 없이도 기존 멱등키가 drivingId 기반이라 영향 없습니다.
        processDrivingSummary(null, dto);
    }
}