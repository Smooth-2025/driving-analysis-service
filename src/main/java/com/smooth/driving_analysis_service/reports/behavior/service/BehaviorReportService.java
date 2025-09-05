package com.smooth.driving_analysis_service.reports.behavior.service;

import com.smooth.driving_analysis_service.reports.behavior.dto.response.BehaviorAnalysisResponseDto;

public interface BehaviorReportService {

    /** Task 1: totalCounts 구현 */
    BehaviorAnalysisResponseDto getBehaviorAnalysis(String reportId);

    // TODO: Task 2, 3에서 배치 스냅샷 메서드들 추가 예정
    // void createOrUpdateInterimSnapshot(Long reportId);
    // void createFinalSnapshot(Long reportId);
}