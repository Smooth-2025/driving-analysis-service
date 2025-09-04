package com.smooth.driving_analysis_service.reports.behavior.service;

import com.smooth.driving_analysis_service.reports.behavior.dto.request.BehaviorDiffRequestDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.*;
import java.util.List;

public interface BehaviorReportService {

    /** 새로운 통합 위험운전 행동 분석 API */
    BehaviorAnalysisResponseDto getBehaviorAnalysis(String reportId);

    // === 기존 메서드들 (하위 호환성) ===
    BehaviorSummaryResponseDto getSummary(Long reportId);

    BehaviorTrajectoryResponseDto getTrajectory(Long reportId);

    List<BehaviorDiffResponseDto> getDiff(Long reportId, BehaviorDiffRequestDto req);

    BehaviorCommentResponseDto getComment(Long reportId, BehaviorDiffRequestDto req);
}
