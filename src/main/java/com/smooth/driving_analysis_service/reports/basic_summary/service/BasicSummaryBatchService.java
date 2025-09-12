package com.smooth.driving_analysis_service.reports.basic_summary.service;

import java.util.List;

public interface BasicSummaryBatchService {
    
    /**
     * 사이클 기반 분석 (기본 요약 통계)
     * @param reportId 리포트 ID (INTERIM/FINAL 구분 포함)
     * @param userId 사용자 ID
     * @param drivingIds 분석 대상 주행 ID 목록 (1~15개)
     */
    void materializeByDrivingIds(String reportId, Long userId, List<String> drivingIds);
}