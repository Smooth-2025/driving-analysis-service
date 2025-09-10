package com.smooth.driving_analysis_service.reports.behavior.service;

import com.smooth.driving_analysis_service.reports.behavior.dto.response.BehaviorAnalysisResponseDto;

import java.util.List;

public interface BehaviorReportService {
    BehaviorAnalysisResponseDto getBehaviorAnalysis(String reportId);
    
    // Task 1: totalCounts - 실시간 집계 데이터
    BehaviorAnalysisResponseDto.TotalCounts calculateTotalCounts(List<String> drivingIds);
    
    // Task 2: drivingPattern - 배치 분석 (Athena)
    BehaviorAnalysisResponseDto.DrivingPattern analyzeDrivingPattern(List<String> drivingIds);
    
    // Task 3: compare - 이전 vs 현재 비교
    BehaviorAnalysisResponseDto.Compare compareWithPrevious(String reportId, BehaviorAnalysisResponseDto.TotalCounts currentCounts);

    void generateInterimReport(Long reportId, Long userId, List<String> drivingIds);
    void generateFinalReport(Long reportId, Long userId, List<String> drivingIds);
}