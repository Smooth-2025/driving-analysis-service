package com.smooth.driving_analysis_service.batch.service;

import com.smooth.driving_analysis_service.batch.dto.ReportTriggerV1;

/**
 * 배치 리포트 처리 서비스 인터페이스
 */
public interface BatchReportService {
    
    /**
     * 리포트 트리거 처리
     * @param trigger 리포트 트리거 정보
     */
    void processReportTrigger(ReportTriggerV1 trigger);
    
    /**
     * 스케줄링된 배치 처리
     */
    void processPendingReports();
}