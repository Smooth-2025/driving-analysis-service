package com.smooth.driving_analysis_service.reports.behavior.service;

import com.smooth.driving_analysis_service.reports.behavior.dto.request.BehaviorDiffRequestDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.*;
import java.util.List;

public interface BehaviorReportService {

    BehaviorSummaryResponseDto getSummary(Long reportId);

    BehaviorTrajectoryResponseDto getTrajectory(Long reportId);

    List<BehaviorDiffResponseDto> getDiff(Long reportId, BehaviorDiffRequestDto req);

    BehaviorCommentResponseDto getComment(Long reportId, BehaviorDiffRequestDto req);
}
