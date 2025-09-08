package com.smooth.driving_analysis_service.reports.milestone.service;

import com.smooth.driving_analysis_service.reports.milestone.dto.response.MilestoneReportResponse;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneItem;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneItemRepository;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import com.smooth.driving_analysis_service.batch.dto.ReportTriggerV1;
import com.smooth.driving_analysis_service.trigger.producer.ReportTriggerProducer;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MilestoneServiceImpl implements MilestoneService {

    private final MilestoneReportRepository milestoneReportRepository;
    private final MilestoneItemRepository milestoneItemRepository;
    private final ReportTriggerProducer reportTriggerProducer;
    private final RedisTemplate<String, String> redisTemplate;

    // 마일스톤 임계값
    private static final int FINAL_MILESTONE = 15;
    private static final List<Integer> INTERIM_MILESTONES = List.of(4, 8, 12);
    
    // Redis 캐시 TTL
    private static final int ACTIVE_REPORT_TTL_DAYS = 30;

    @Override
    @Transactional(readOnly = true)
    public List<MilestoneReportResponse> listByUser(long userId) {
        return milestoneReportRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(MilestoneReportResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MilestoneReportResponse getStamp(long id) {
        var r = milestoneReportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 마일스톤을 찾을 수 없습니다. id=" + id));
        return MilestoneReportResponse.builder()
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
    public MilestoneReportResponse getStampByUserId(long userId) {
        var activeReport = getActiveReport(userId);
        if (activeReport.isPresent()) {
            var r = activeReport.get();
            return MilestoneReportResponse.builder()
                    .reportId(r.getReportId())
                    .numberOfDriving(r.getNumberOfDriving())
                    .build();
        }
        
        // 활성 리포트가 없으면 기본값 반환
        return MilestoneReportResponse.builder()
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
            
            // reportId가 "u{userId}_r{cycleNo}_{date}" 형식인 경우
            String[] parts = reportId.split("_");
            if (parts.length >= 2) {
                Long userId = Long.parseLong(parts[0].substring(1)); // "u123" -> 123
                Integer cycleNo = Integer.parseInt(parts[1].substring(1)); // "r1" -> 1
                
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
        } catch (NumberFormatException e) {
            throw new EntityNotFoundException("해당 마일스톤을 찾을 수 없습니다. reportId=" + reportId);
        } catch (Exception e) {
            throw new EntityNotFoundException("해당 마일스톤을 찾을 수 없습니다. reportId=" + reportId);
        }
    }

    /**
     * 주행 완료 시 마일스톤 관리
     * 1. 현재 활성 리포트 조회/생성
     * 2. MilestoneItem 추가
     * 3. 마일스톤 도달 시 트리거 발행
     */
    @Override
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
        
        // 4. 마일스톤 도달 체크 및 트리거 발행
        checkAndTriggerMilestone(activeReport, nextOrderNo);
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
            redisTemplate.opsForValue().set(cacheKey, String.valueOf(activeReport.get().getId()), 
                    ACTIVE_REPORT_TTL_DAYS, TimeUnit.DAYS);
            return activeReport.get();
        }
        
        // 새 리포트 생성
        return createNewReport(userId, cacheKey);
    }

    /**
     * 새 마일스톤 리포트 생성
     */
    private MilestoneReport createNewReport(Long userId, String cacheKey) {
        // 다음 사이클 번호 계산 (기존 메서드 사용)
        Optional<MilestoneReport> latestReport = milestoneReportRepository.findTopByUserIdOrderByCycleNoDesc(userId);
        int nextCycleNo = latestReport.map(r -> (r.getCycleNo() == null ? 0 : r.getCycleNo()) + 1).orElse(1);
        
        MilestoneReport newReport = MilestoneReport.newCollecting(userId, nextCycleNo);
        MilestoneReport saved = milestoneReportRepository.save(newReport);
        
        // 캐시 저장 (ID로 저장)
        redisTemplate.opsForValue().set(cacheKey, String.valueOf(saved.getId()), 
                ACTIVE_REPORT_TTL_DAYS, TimeUnit.DAYS);
        
        log.info("Created new milestone report: reportId={}, cycleNo={}", 
                saved.getReportId(), nextCycleNo);
        
        return saved;
    }

    /**
     * 마일스톤 도달 체크 및 트리거 발행
     */
    private void checkAndTriggerMilestone(MilestoneReport report, int numberOfDriving) {
        boolean shouldTrigger = false;
        String triggerType = null;
        
        if (numberOfDriving == FINAL_MILESTONE) {
            // 15회 달성 → FINAL 트리거
            shouldTrigger = true;
            triggerType = "FINAL";
            
            // 상태 변경: COLLECTING → PROCESSING
            report.setStatus(MilestoneReport.Status.PROCESSING);
            milestoneReportRepository.save(report);
            
            // active-report 캐시 삭제 (새 사이클 시작 준비)
            String cacheKey = com.smooth.driving_analysis_service.global.redis.RedisKeys.activeReportForUser(report.getUserId());
            redisTemplate.delete(cacheKey);
            
            log.info("Final milestone reached: reportId={}", report.getReportId());
            
        } else if (INTERIM_MILESTONES.contains(numberOfDriving)) {
            // 4/8/12회 달성 → INTERIM 트리거
            shouldTrigger = true;
            triggerType = "INTERIM";
            
            log.info("Interim milestone reached: reportId={}, milestone={}", 
                    report.getReportId(), numberOfDriving);
        }
        
        if (shouldTrigger) {
            emitReportTrigger(report, triggerType, numberOfDriving);
        }
    }

    /**
     * report.trigger 스트림에 트리거 발행
     */
    private void emitReportTrigger(MilestoneReport report, String type, int milestone) {
        // 해당 리포트의 모든 drivingId 조회
        List<String> drivingIds = milestoneItemRepository
                .findByReportIdOrderByOrderNoAsc(report.getId())
                .stream()
                .map(MilestoneItem::getDrivingId)
                .toList();
        
        com.smooth.driving_analysis_service.batch.dto.ReportTriggerV1 trigger = 
                com.smooth.driving_analysis_service.batch.dto.ReportTriggerV1.builder()
                .v(1)
                .type(type)
                .userId(String.valueOf(report.getUserId()))
                .reportId(report.getId())
                .milestone(milestone)
                .drivingIds(drivingIds)
                .status(report.getStatus().name())
                .emittedAt(LocalDateTime.now())
                .producer("milestone-service")
                .traceId(UUID.randomUUID().toString())
                .build();
        
        reportTriggerProducer.emit(trigger);
        
        log.info("Report trigger emitted: type={}, reportId={}, milestone={}, drivingCount={}", 
                type, report.getReportId(), milestone, drivingIds.size());
    }

    /**
     * 리포트 상태를 COMPLETED로 변경 (배치에서 호출)
     */
    @Override
    public void markReportCompleted(Long reportId) {
        MilestoneReport report = milestoneReportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Report not found: " + reportId));
        
        if (report.getStatus() != MilestoneReport.Status.PROCESSING) {
            log.warn("Report is not in PROCESSING status: reportId={}, status={}", 
                    reportId, report.getStatus());
            return;
        }
        
        report.setStatus(MilestoneReport.Status.COMPLETED);
        milestoneReportRepository.save(report);
        
        log.info("Report marked as completed: reportId={}", report.getReportId());
    }

    /**
     * 사용자의 현재 활성 리포트 조회
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<MilestoneReport> getActiveReport(Long userId) {
        return milestoneReportRepository.findFirstByUserIdAndStatusOrderByIdDesc(userId, MilestoneReport.Status.COLLECTING);
    }

    /**
     * 오래된 PROCESSING 상태 리포트 정리
     * 24시간 이상 PROCESSING 상태인 리포트를 FAILED로 변경
     */
    @Override
    @Transactional
    public void cleanupStaleProcessingReports() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        
        List<MilestoneReport> staleReports = milestoneReportRepository
                .findByStatusAndUpdatedAtBefore(MilestoneReport.Status.PROCESSING, cutoffTime);
        
        if (!staleReports.isEmpty()) {
            log.info("Found {} stale PROCESSING reports, marking as FAILED", staleReports.size());
            
            for (MilestoneReport report : staleReports) {
                report.setStatus(MilestoneReport.Status.FAILED);
                log.warn("Marked stale report as FAILED: reportId={}, updatedAt={}", 
                        report.getReportId(), report.getUpdatedAt());
            }
            
            milestoneReportRepository.saveAll(staleReports);
        }
    }
}
