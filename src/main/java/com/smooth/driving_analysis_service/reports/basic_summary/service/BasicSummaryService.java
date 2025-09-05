package com.smooth.driving_analysis_service.reports.basic_summary.service;

import com.smooth.driving_analysis_service.reports.basic_summary.dto.BasicSummaryResponse;

public interface BasicSummaryService {
    
    /**
     * 중간 리포트 생성/갱신 (INTERIM)
     */
    void generateInterimReport(Long reportId, Long userId);
    
    /**
     * 최종 리포트 생성 (FINAL)
     */
    void generateFinalReport(Long reportId, Long userId);
    
    /**
     * 리포트 상단 요약 조회
     */
    BasicSummaryResponse getBasicSummary(Long reportId);
}