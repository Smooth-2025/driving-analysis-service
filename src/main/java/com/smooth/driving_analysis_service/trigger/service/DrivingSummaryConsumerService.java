package com.smooth.driving_analysis_service.trigger.service;

import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneItem;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneItemRepository;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import com.smooth.driving_analysis_service.global.redis.RedisKeys;
import com.smooth.driving_analysis_service.trigger.dto.DrivingSummaryV1;
import com.smooth.driving_analysis_service.batch.dto.ReportTriggerV1;
import com.smooth.driving_analysis_service.trigger.producer.ReportTriggerProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrivingSummaryConsumerService {

    private final RedisTemplate<String, String> redis;
    private final MilestoneReportRepository reportRepo;
    private final MilestoneItemRepository itemRepo;
    private final ReportTriggerProducer producer;

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

        // 4) 중간 분석 트리거 (4/8/12회) 및 최종 분석 트리거 (15회)
        int count = itemRepo.countByReportId(report.getId());
        
        // 중간 분석: 4, 8, 12회 도달 시
        if ((count == 4 || count == 8 || count == 12) && report.getStatus() == MilestoneReport.Status.COLLECTING) {
            List<String> tripIds = itemRepo.findByReportIdOrderByOrderNoAsc(report.getId())
                    .stream()
                    .map(MilestoneItem::getDrivingId)
                    .toList();

            ReportTriggerV1 trigger = ReportTriggerV1.builder()
                    .v(1)
                    .userId(String.valueOf(userId))
                    .reportId(String.valueOf(report.getId()))
                    .milestone(String.valueOf(count))
                    .drivingIds(tripIds)
                    .status("COLLECTING")
                    .type("INTERIM")
                    .build();

            producer.emit(trigger);
            log.info("INTERIM trigger emitted: reportId={}, milestone={}", report.getId(), count);
        }
        
        // 최종 분석: 15회 도달 시
        if (count >= threshold && report.getStatus() == MilestoneReport.Status.COLLECTING) {

            report.setStatus(MilestoneReport.Status.PROCESSING);
            report.setNumberOfDriving(count);
            reportRepo.save(report);

            // active-report 캐시 제거 (다음 트립부터는 새 사이클로)
            redis.delete(RedisKeys.activeReportForUser(String.valueOf(userId)));

            // 트리거 발행
            List<String> tripIds = itemRepo.findByReportIdOrderByOrderNoAsc(report.getId())
                    .stream()
                    .map(MilestoneItem::getDrivingId)
                    .toList();

            ReportTriggerV1 trigger = ReportTriggerV1.builder()
                    .v(1)
                    .userId(String.valueOf(userId))
                    .reportId(String.valueOf(report.getId()))
                    .milestone(String.valueOf(threshold))
                    .drivingIds(tripIds)
                    .status("PROCESSING")
                    .type("FINAL")
                    .build();

            producer.emit(trigger);
            log.info("FINAL trigger emitted: reportId={}, tripCount={}", report.getId(), count);
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