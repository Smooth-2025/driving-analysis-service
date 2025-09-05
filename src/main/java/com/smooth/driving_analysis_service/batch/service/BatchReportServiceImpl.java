package com.smooth.driving_analysis_service.batch.service;

import com.smooth.driving_analysis_service.reports.basic_summary.service.BasicSummaryService;
import com.smooth.driving_analysis_service.reports.behavior.service.BehaviorReportService;
import com.smooth.driving_analysis_service.reports.milestone.service.MilestoneService;
import com.smooth.driving_analysis_service.trigger.dto.ReportTriggerV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchReportServiceImpl implements BatchReportService {

    private final BasicSummaryService basicSummaryService;
    private final BehaviorReportService behaviorReportService;
    private final MilestoneService milestoneService;

    @Override
    @Transactional
    public void processReportTrigger(ReportTriggerV1 trigger) {
        log.info("Processing report trigger: type={}, reportId={}, milestone={}", 
                trigger.getType(), trigger.getReportId(), trigger.getMilestone());
        
        try {
            if ("INTERIM".equals(trigger.getType())) {
                processInterimReport(trigger);
            } else if ("FINAL".equals(trigger.getType())) {
                processFinalReport(trigger);
            } else {
                log.warn("Unknown trigger type: {}", trigger.getType());
            }
        } catch (Exception e) {
            log.error("Failed to process report trigger: reportId={}", trigger.getReportId(), e);
            throw e; // 재시도를 위해 예외를 다시 던짐
        }
    }

    /**
     * 중간 분석 처리 (4/8/12회)
     * 기존 INTERIM 스냅샷을 덮어쓰기
     */
    private void processInterimReport(ReportTriggerV1 trigger) {
        log.info("Processing interim report: reportId={}, milestone={}", 
                trigger.getReportId(), trigger.getMilestone());
        
        Long reportId = trigger.getReportId();
        Long userId = Long.parseLong(trigger.getUserId());
        
        // 각 리포트 서비스 호출 (INTERIM 타입으로 생성/갱신)
        try {
            // 1. Basic Summary (누적 통계 기반)
            basicSummaryService.generateInterimReport(reportId, userId);
            
            // 2. Behavior Analysis (누적 통계 + S3 데이터)
            // behaviorReportService.generateInterimReport(reportId, userId, trigger.getDrivingIds());
            
            // 3. DNA Analysis
            // dnaReportService.generateInterimReport(reportId, userId, trigger.getDrivingIds());
            
            // 4. Accident Reaction Analysis  
            // accidentReactionService.generateInterimReport(reportId, userId, trigger.getDrivingIds());
            
            log.info("Interim report processing completed: reportId={}", reportId);
            
        } catch (Exception e) {
            log.error("Failed to process interim report: reportId={}", reportId, e);
            throw e;
        }
    }

    /**
     * 최종 분석 처리 (15회)
     * FINAL 스냅샷 생성 후 상태를 COMPLETED로 변경
     */
    private void processFinalReport(ReportTriggerV1 trigger) {
        log.info("Processing final report: reportId={}, milestone={}", 
                trigger.getReportId(), trigger.getMilestone());
        
        Long reportId = trigger.getReportId();
        Long userId = Long.parseLong(trigger.getUserId());
        
        try {
            // 각 리포트 서비스 호출 (FINAL 타입으로 생성)
            // 1. Basic Summary
            basicSummaryService.generateFinalReport(reportId, userId);
            
            // 2. Behavior Analysis
            // behaviorReportService.generateFinalReport(reportId, userId, trigger.getDrivingIds());
            
            // 3. DNA Analysis
            // dnaReportService.generateFinalReport(reportId, userId, trigger.getDrivingIds());
            
            // 4. Accident Reaction Analysis
            // accidentReactionService.generateFinalReport(reportId, userId, trigger.getDrivingIds());
            
            // 5. 마일스톤 상태를 COMPLETED로 변경
            milestoneService.markReportCompleted(reportId);
            
            log.info("Final report processing completed: reportId={}", reportId);
            
        } catch (Exception e) {
            log.error("Failed to process final report: reportId={}", reportId, e);
            throw e;
        }
    }

    /**
     * 스케줄링된 배치 처리
     * 개발: 10분마다, 운영: 새벽 2시
     */
    @Scheduled(fixedRate = 600000) // 10분 (개발용)
    // @Scheduled(cron = "0 0 2 * * *") // 새벽 2시 (운영용)
    @Override
    public void processPendingReports() {
        log.debug("Checking for pending report triggers...");
        
        // 실제로는 report.trigger 스트림에서 실시간으로 처리되므로
        // 여기서는 실패한 트리거들을 재처리하거나 정리 작업을 수행
        
        // TODO: 실패한 트리거 재처리 로직
        // TODO: 오래된 스트림 메시지 정리
    }
}