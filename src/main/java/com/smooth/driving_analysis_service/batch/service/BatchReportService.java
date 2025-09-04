package com.smooth.driving_analysis_service.batch.service;

import com.smooth.driving_analysis_service.batch.dto.ReportTriggerV1;
import com.smooth.driving_analysis_service.reports.basic_summary.service.BasicSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchReportService {
    
    private final BasicSummaryService basicSummaryService;
    
    @Scheduled(fixedRate = 600000) // 개발: 10분
    // @Scheduled(cron = "0 0 2 * * *") // 운영: 새벽 2시
    public void processPendingReports() {
        log.info("배치 리포트 처리 시작");
        // report.trigger 스트림에서 대기 중인 트리거 처리
        // 현재는 ReportTriggerConsumer에서 실시간 처리하므로 추가 로직 없음
        log.info("배치 리포트 처리 완료");
    }
    
    @Transactional
    public void processReportTrigger(ReportTriggerV1 trigger) {
        log.info("리포트 트리거 처리 시작 - type: {}, reportId: {}, milestone: {}", 
                trigger.getType(), trigger.getReportId(), trigger.getMilestone());
        
        try {
            if ("INTERIM".equals(trigger.getType())) {
                processInterimReport(trigger);
            } else if ("FINAL".equals(trigger.getType())) {
                processFinalReport(trigger);
            }
        } catch (Exception e) {
            log.error("리포트 트리거 처리 실패 - reportId: {}", trigger.getReportId(), e);
            throw e;
        }
    }
    
    private void processInterimReport(ReportTriggerV1 trigger) {
        log.info("중간 리포트 처리 시작 - reportId: {}, milestone: {}", 
                trigger.getReportId(), trigger.getMilestone());
        
        Long reportId = Long.parseLong(trigger.getReportId());
        
        // basic-summary INTERIM 스냅샷 생성/갱신
        basicSummaryService.createOrUpdateInterimSnapshot(reportId);
        
        // TODO: behavior, dna, accident_reaction 서비스 호출
        
        log.info("중간 리포트 처리 완료 - reportId: {}", trigger.getReportId());
    }
    
    private void processFinalReport(ReportTriggerV1 trigger) {
        log.info("최종 리포트 처리 시작 - reportId: {}", trigger.getReportId());
        
        Long reportId = Long.parseLong(trigger.getReportId());
        
        // basic-summary FINAL 스냅샷 생성
        basicSummaryService.createFinalSnapshot(reportId);
        
        // TODO: behavior, dna, accident_reaction 서비스 호출
        // TODO: milestone_report.status = COMPLETED 업데이트
        
        log.info("최종 리포트 처리 완료 - reportId: {}", trigger.getReportId());
    }
}