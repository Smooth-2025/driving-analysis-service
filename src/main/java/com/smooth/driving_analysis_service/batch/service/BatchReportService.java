package com.smooth.driving_analysis_service.batch.service;

import com.smooth.driving_analysis_service.trigger.dto.ReportTriggerV1;

public interface BatchReportService {
    
    /**
     * report.trigger 스트림에서 받은 트리거 처리
     */
    void processReportTrigger(ReportTriggerV1 trigger);
    
    /**
     * 스케줄링된 배치 처리 (대기 중인 트리거들 처리)
     */
    void processPendingReports();
}