package com.smooth.driving_analysis_service.batch.service;

import com.smooth.driving_analysis_service.batch.dto.ReportTriggerV1;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchReportService {

    private final MilestoneReportRepository milestoneReportRepository;
    private final com.smooth.driving_analysis_service.reports.dna.service.DnaBatchService dnaBatchService;
    // TODO: reports 패키지 서비스들 주입 예정
    // private final BasicSummaryService basicSummaryService;
    // private final BehaviorReportService behaviorReportService;
    // private final AccidentReactionService accidentReactionService;

    /**
     * 개발환경: 10분마다 실행
     * 운영환경: 새벽 2시 실행 (주석 해제 필요)
     */
    @Scheduled(fixedRate = 600000) // 10분 = 600,000ms
    // @Scheduled(cron = "0 0 2 * * *") // 매일 새벽 2시
    public void processPendingReports() {
        log.info("[BATCH] Starting scheduled batch processing");
        
        try {
            // PROCESSING 상태인 리포트들을 찾아서 처리
            List<MilestoneReport> processingReports = milestoneReportRepository
                    .findByStatus(MilestoneReport.Status.PROCESSING);
            
            log.info("[BATCH] Found {} reports in PROCESSING status", processingReports.size());
            
            for (MilestoneReport report : processingReports) {
                try {
                    processReport(report);
                } catch (Exception e) {
                    log.error("[BATCH] Failed to process report: reportId={}, error={}", 
                            report.getId(), e.getMessage(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("[BATCH] Scheduled batch processing failed: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public void processReportTrigger(ReportTriggerV1 trigger) {
        log.info("[BATCH] Processing report trigger: type={}, reportId={}, milestone={}", 
                trigger.getType(), trigger.getReportId(), trigger.getMilestone());

        if (trigger.isInterim()) {
            processInterimReport(trigger);
        } else if (trigger.isFinal()) {
            processFinalReport(trigger);
        } else {
            log.warn("[BATCH] Unknown trigger type: {}", trigger.getType());
        }
    }

    private void processInterimReport(ReportTriggerV1 trigger) {
        log.info("[BATCH] Processing INTERIM report: reportId={}, milestone={}", 
                trigger.getReportId(), trigger.getMilestone());

        try {
            // TODO: 각 리포트 서비스 호출
            // basicSummaryService.generateInterimSummary(trigger.getReportId());
            // behaviorReportService.generateInterimReport(trigger.getReportId());
            dnaBatchService.runInterim(trigger.getReportId());
            // accidentReactionService.generateInterimReport(trigger.getReportId());
            
            log.info("[BATCH] INTERIM report processing completed: reportId={}", trigger.getReportId());
            
        } catch (Exception e) {
            log.error("[BATCH] INTERIM report processing failed: reportId={}, error={}", 
                    trigger.getReportId(), e.getMessage(), e);
            throw e;
        }
    }

    private void processFinalReport(ReportTriggerV1 trigger) {
        log.info("[BATCH] Processing FINAL report: reportId={}, milestone={}", 
                trigger.getReportId(), trigger.getMilestone());

        try {
            // TODO: 각 리포트 서비스 호출
            // basicSummaryService.generateFinalSummary(trigger.getReportId());
            // behaviorReportService.generateFinalReport(trigger.getReportId());
            dnaBatchService.runFinal(trigger.getReportId());
            // accidentReactionService.generateFinalReport(trigger.getReportId());

            // 상태를 COMPLETED로 변경
            MilestoneReport report = milestoneReportRepository.findById(trigger.getReportId())
                    .orElseThrow(() -> new IllegalArgumentException("Report not found: " + trigger.getReportId()));
            
            report.setStatus(MilestoneReport.Status.COMPLETED);
            milestoneReportRepository.save(report);
            
            log.info("[BATCH] FINAL report processing completed: reportId={}", trigger.getReportId());
            
        } catch (Exception e) {
            log.error("[BATCH] FINAL report processing failed: reportId={}, error={}", 
                    trigger.getReportId(), e.getMessage(), e);
            throw e;
        }
    }

    private void processReport(MilestoneReport report) {
        log.info("[BATCH] Processing scheduled report: reportId={}, numberOfDriving={}", 
                report.getId(), report.getNumberOfDriving());

        // 15개 달성한 리포트만 최종 처리
        if (report.getNumberOfDriving() == 15) {
            ReportTriggerV1 trigger = ReportTriggerV1.builder()
                    .type("FINAL")
                    .userId(report.getUserId().toString())
                    .reportId(report.getId())
                    .milestone(15)
                    .status("PROCESSING")
                    .build();
            
            processFinalReport(trigger);
        }
    }
}