package com.smooth.driving_analysis_service.reports.behavior.service;

import java.util.List;

public interface BehaviorBatchService {
    
    /**
     * 사이클 기반 분석 (위험 행동 분석)
     * @param reportId 리포트 ID (INTERIM/FINAL 구분 포함)
     * @param userId 사용자 ID
     * @param drivingIds 분석 대상 주행 ID 목록 (1~15개)
     */
    void materializeByDrivingIds(String reportId, Long userId, List<String> drivingIds);
}