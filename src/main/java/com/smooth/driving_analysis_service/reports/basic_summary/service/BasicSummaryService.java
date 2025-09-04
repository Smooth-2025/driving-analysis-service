package com.smooth.driving_analysis_service.reports.basic_summary.service;

import com.smooth.driving_analysis_service.reports.basic_summary.dto.BasicSummaryResponse;

public interface BasicSummaryService {
    
    /**
     * 리포트 ID로 기본 통계 조회
     */
    BasicSummaryResponse getBasicSummary(Long reportId);
    
    /**
     * INTERIM 스냅샷 생성/갱신
     */
    void createOrUpdateInterimSnapshot(Long reportId);
    
    /**
     * FINAL 스냅샷 생성
     */
    void createFinalSnapshot(Long reportId);
}