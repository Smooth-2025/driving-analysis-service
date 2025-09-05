package com.smooth.driving_analysis_service.batch.service;

import com.smooth.driving_analysis_service.batch.dto.ReportTriggerV1;
import com.smooth.driving_analysis_service.reports.basic_summary.service.BasicSummaryService;
import com.smooth.driving_analysis_service.reports.behavior.service.BehaviorReportService;
import com.smooth.driving_analysis_service.reports.milestone.service.MilestoneService;
import com.smooth.driving_analysis_service.reports.accident_reaction.service.AccidentReactionBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 배치 리포트 처리 서비스
 * - report.trigger 스트림 소비하여 리포트 생성
 * - 스케줄링된 배치 처리 (개발: 10분, 운영: 새벽 2시)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchReportServiceImpl implements BatchReportService {

    private final BasicSummaryService basicSummaryService;
    private final BehaviorReportService behaviorReportService;
    private final MilestoneService milestoneService;
    private final AccidentReactionBatchService accidentReactionBatchService;
    // TODO: DNA 서비스 추가

    @Override
    @Transactional
    public void processReportTrigger(ReportTriggerV1 trigger) {
        log.info("Processing report trigger: type={}, reportId={}, milestone={}, drivingCount={}", 
                trigger.getType(), trigger.getReportId(), trigger.getMilestone(), 
                trigger.getDrivingIds() != null ? trigger.getDrivingIds().size() : 0);
        
        try {
            if (trigger.isInterim()) {
                processInterimReport(trigger);
            } else if (trigger.isFinal()) {
                processFinalReport(trigger);
            } else {
                log.warn("Unknown trigger type: {}", trigger.getType());
            }
        } catch (Exception e) {
            log.error("Failed to process report trigger: reportId={}, type={}", 
                    trigger.getReportId(), trigger.getType(), e);
            throw e; // 재시도를 위해 예외를 다시 던짐
        }
    }

    /**
     * 중간 분석 처리 (4/8/12회)
     * - 기존 INTERIM 스냅샷을 덮어쓰기
     * - 누적 통계 기반 + S3 상세 데이터 분석
     */
    private void processInterimReport(ReportTriggerV1 trigger) {
        log.info("Processing interim report: reportId={}, milestone={}", 
                trigger.getReportId(), trigger.getMilestone());
        
        Long reportId = trigger.getReportId();
        Long userId = Long.parseLong(trigger.getUserId());
        
        try {
            // 1. Basic Summary (누적 통계 기반)
            basicSummaryService.generateInterimReport(reportId, userId);
            log.debug("Basic summary interim report generated: reportId={}", reportId);
            
            // 2. Behavior Analysis (누적 통계 + S3 데이터)
            // behaviorReportService.generateInterimReport(reportId, userId, trigger.getDrivingIds());
            // log.debug("Behavior interim report generated: reportId={}", reportId);
            
            // 3. DNA Analysis (누적 통계 + S3 데이터)
            // dnaReportService.generateInterimReport(reportId, userId, trigger.getDrivingIds());
            // log.debug("DNA interim report generated: reportId={}", reportId);
            
            // 4. Accident Reaction Analysis (S3 데이터 분석)
            accidentReactionBatchService.generateInterimReport(reportId, userId, trigger.getDrivingIds());
            log.debug("Accident reaction interim report generated: reportId={}", reportId);
            
            log.info("Interim report processing completed: reportId={}, milestone={}", 
                    reportId, trigger.getMilestone());
            
        } catch (Exception e) {
            log.error("Failed to process interim report: reportId={}, milestone={}", 
                    reportId, trigger.getMilestone(), e);
            throw e;
        }
    }

    /**
     * 최종 분석 처리 (15회)
     * - FINAL 스냅샷 생성 (불변)
     * - 마일스톤 상태를 COMPLETED로 변경
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
            log.debug("Basic summary final report generated: reportId={}", reportId);
            
            // 2. Behavior Analysis
            // behaviorReportService.generateFinalReport(reportId, userId, trigger.getDrivingIds());
            // log.debug("Behavior final report generated: reportId={}", reportId);
            
            // 3. DNA Analysis
            // dnaReportService.generateFinalReport(reportId, userId, trigger.getDrivingIds());
            // log.debug("DNA final report generated: reportId={}", reportId);
            
            // 4. Accident Reaction Analysis
            accidentReactionBatchService.generateFinalReport(reportId, userId, trigger.getDrivingIds());
            log.debug("Accident reaction final report generated: reportId={}", reportId);
            
            // 5. 마일스톤 상태를 COMPLETED로 변경 (사용자 노출 가능)
            milestoneService.markReportCompleted(reportId);
            
            log.info("Final report processing completed: reportId={}, milestone={}", 
                    reportId, trigger.getMilestone());
            
        } catch (Exception e) {
            log.error("Failed to process final report: reportId={}, milestone={}", 
                    reportId, trigger.getMilestone(), e);
            throw e;
        }
    }

    /**
     * 스케줄링된 배치 처리
     * 개발: 10분마다, 운영: 새벽 2시
     * 
     * 실제로는 report.trigger 스트림에서 실시간으로 처리되므로
     * 여기서는 실패한 트리거들을 재처리하거나 정리 작업을 수행
     */
    @Scheduled(fixedRate = 600000) // 10분 (개발용)
    // @Scheduled(cron = "0 0 2 * * *") // 새벽 2시 (운영용)
    @Override
    public void processPendingReports() {
        log.debug("Scheduled batch processing started");
        
        try {
            // TODO: 실패한 트리거 재처리 로직
            // 1. report.trigger 스트림에서 처리 실패한 메시지 재시도
            // 2. PROCESSING 상태로 오래 남아있는 리포트 정리
            // 3. 오래된 스트림 메시지 정리
            
            log.debug("Scheduled batch processing completed");
            
        } catch (Exception e) {
            log.error("Scheduled batch processing failed", e);
        }
    }
}