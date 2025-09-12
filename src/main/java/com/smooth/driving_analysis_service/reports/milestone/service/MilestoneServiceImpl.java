package com.smooth.driving_analysis_service.reports.milestone.service;

import com.smooth.driving_analysis_service.reports.milestone.dto.response.MilestoneReportResponseDto;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneItem;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneItemRepository;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import com.smooth.driving_analysis_service.reports.dna.service.DnaBatchService;
import com.smooth.driving_analysis_service.reports.basic_summary.service.BasicSummaryBatchService;
import com.smooth.driving_analysis_service.reports.behavior.service.BehaviorBatchService;
import com.smooth.driving_analysis_service.reports.accident_reaction.service.AccidentReactionBatchService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MilestoneServiceImpl implements MilestoneService {

    private final MilestoneReportRepository milestoneReportRepository;
    private final MilestoneItemRepository milestoneItemRepository;
    private final RedisTemplate<String, String> redisTemplate;
    
    // 배치 서비스들
    private final DnaBatchService dnaBatchService;
    private final BasicSummaryBatchService basicSummaryBatchService;
    private final BehaviorBatchService behaviorBatchService;
    private final AccidentReactionBatchService accidentReactionBatchService;

    private static final int ACTIVE_REPORT_TTL_DAYS = 30;

    @Override
    @Transactional(readOnly = true)
    public List<MilestoneReportResponseDto> listByUser(long userId) {
        return milestoneReportRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(MilestoneReportResponseDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MilestoneReportResponseDto getStamp(long id) {
        var r = milestoneReportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 마일스톤을 찾을 수 없습니다. id=" + id));
        return MilestoneReportResponseDto.builder()
                .id(r.getId())
                .numberOfDriving(r.getNumberOfDriving())
                .build();
    }

    @Override
    public void updateRead(long id, boolean read) {
        var r = milestoneReportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 마일스톤을 찾을 수 없습니다. id=" + id));
        r.setRead(read);
        milestoneReportRepository.save(r);
    }

    @Override
    @Transactional(readOnly = true)
    public MilestoneReportResponseDto getStampByUserId(long userId) {
        var activeReport = getActiveReport(userId);
        if (activeReport.isPresent()) {
            var r = activeReport.get();
            return MilestoneReportResponseDto.builder()
                    .id(r.getId())
                    .reportId(r.getReportId())
                    .numberOfDriving(r.getNumberOfDriving())
                    .build();
        }

        // 활성 리포트가 없으면 기본값 반환
        return MilestoneReportResponseDto.builder()
                .id(0L)
                .reportId("report_123") // 기본값
                .numberOfDriving(0)
                .build();
    }

    @Override
    @Transactional
    public MilestoneReport updateReadByReportId(String reportId, boolean read) {
        try {
            // reportId가 숫자인 경우 직접 ID로 처리
            if (reportId.matches("\\d+")) {
                Long id = Long.parseLong(reportId);
                var r = milestoneReportRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("해당 마일스톤을 찾을 수 없습니다. reportId=" + reportId));
                r.setRead(read);
                return milestoneReportRepository.save(r);
            }

            // "u123_r1" 형식 파싱
            if (reportId.contains("_r")) {
                String[] parts = reportId.split("_r");
                Long userId = Long.parseLong(parts[0].substring(1)); // "u123" -> 123
                Integer cycleNo = Integer.parseInt(parts[1]); // "1" -> 1

                var reports = milestoneReportRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
                var r = reports.stream()
                        .filter(report -> report.getCycleNo().equals(cycleNo))
                        .findFirst()
                        .orElseThrow(() -> new EntityNotFoundException("해당 마일스톤을 찾을 수 없습니다. reportId=" + reportId));

                r.setRead(read);
                return milestoneReportRepository.save(r);
            } else {
                throw new IllegalArgumentException("잘못된 reportId 형식입니다: " + reportId);
            }
        } catch (Exception e) {
            log.error("Failed to update read status for reportId: {}", reportId, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void processDrivingCompleted(Long userId, String drivingId) {
        log.info("Processing driving completed: userId={}, drivingId={}", userId, drivingId);

        // 1. 현재 활성 리포트 조회/생성
        MilestoneReport activeReport = getOrCreateActiveReport(userId);

        // 2. MilestoneItem 추가
        int nextOrderNo = activeReport.getNumberOfDriving() + 1;
        MilestoneItem item = MilestoneItem.of(activeReport, drivingId, nextOrderNo);
        milestoneItemRepository.save(item);

        // 3. numberOfDriving 증가
        activeReport.setNumberOfDriving(nextOrderNo);
        milestoneReportRepository.save(activeReport);

        log.info("Milestone updated: reportId={}, numberOfDriving={}",
                activeReport.getReportId(), nextOrderNo);

        // 4. 15회 도달 시 상태만 변경 (스케줄러에서 FINAL 생성)
        if (nextOrderNo >= 15) {
            activeReport.setStatus(MilestoneReport.Status.PROCESSING);
            milestoneReportRepository.save(activeReport);
            log.info("Milestone report marked as PROCESSING: reportId={} - will be finalized by scheduler", activeReport.getReportId());
        }
    }

    @Override
    @Transactional
    public void markReportCompleted(Long reportId) {
        MilestoneReport report = milestoneReportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Report not found: " + reportId));
        
        report.setStatus(MilestoneReport.Status.COMPLETED);
        milestoneReportRepository.save(report);
        
        log.info("Milestone report marked as COMPLETED: reportId={}", reportId);
    }

    /**
     * 현재 활성 리포트 조회 또는 새로 생성
     */
    private MilestoneReport getOrCreateActiveReport(Long userId) {
        String cacheKey = com.smooth.driving_analysis_service.global.redis.RedisKeys.activeReportForUser(userId);
        String cachedReportId = redisTemplate.opsForValue().get(cacheKey);

        if (cachedReportId != null) {
            try {
                Long reportId = Long.parseLong(cachedReportId);
                Optional<MilestoneReport> cached = milestoneReportRepository.findById(reportId);
                if (cached.isPresent() && cached.get().getStatus() == MilestoneReport.Status.COLLECTING) {
                    return cached.get();
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid cached reportId: {}", cachedReportId);
            }
        }

        // 캐시 미스 또는 상태 변경 → DB에서 활성 리포트 조회
        Optional<MilestoneReport> activeReport = milestoneReportRepository
                .findFirstByUserIdAndStatusOrderByIdDesc(userId, MilestoneReport.Status.COLLECTING);

        if (activeReport.isPresent()) {
            // 캐시 갱신
            redisTemplate.opsForValue().set(cacheKey, activeReport.get().getId().toString(),
                    ACTIVE_REPORT_TTL_DAYS, TimeUnit.DAYS);
            return activeReport.get();
        }

        // 활성 리포트가 없으면 새로 생성
        return createNewReport(userId, cacheKey);
    }

    /**
     * 새 마일스톤 리포트 생성
     */
    private MilestoneReport createNewReport(Long userId, String cacheKey) {
        // 다음 사이클 번호 계산
        Optional<MilestoneReport> latestReport = milestoneReportRepository.findTopByUserIdOrderByCycleNoDesc(userId);
        int nextCycleNo = latestReport.map(r -> (r.getCycleNo() == null ? 0 : r.getCycleNo()) + 1).orElse(1);

        MilestoneReport newReport = MilestoneReport.newCollecting(userId, nextCycleNo);
        MilestoneReport saved = milestoneReportRepository.save(newReport);

        // 캐시 저장 (ID로 저장)
        redisTemplate.opsForValue().set(cacheKey, saved.getId().toString(),
                ACTIVE_REPORT_TTL_DAYS, TimeUnit.DAYS);

        log.info("Created new milestone report: reportId={}, cycleNo={}",
                saved.getReportId(), nextCycleNo);

        return saved;
    }

    private Optional<MilestoneReport> getActiveReport(Long userId) {
        return milestoneReportRepository.findFirstByUserIdAndStatusOrderByIdDesc(userId, MilestoneReport.Status.COLLECTING);
    }
    

}