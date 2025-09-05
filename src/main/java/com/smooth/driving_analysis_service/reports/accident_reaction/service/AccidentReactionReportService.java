package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.AccidentReactionBasicMetricsDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.AccidentReactionReportResponseDto;

public interface AccidentReactionReportService {
    
    /**
     * Task 1: 기본 반응 지표 조회
     * @param reportId 마일스톤 리포트 ID
     * @return 기본 반응 지표
     */
    AccidentReactionBasicMetricsDto getBasicMetrics(String reportId);
    
    /**
     * 전체 리포트 응답 생성 (Task 1 + Task 2)
     * @param reportId 마일스톤 리포트 ID
     * @return 완전한 리포트 응답
     */
    AccidentReactionReportResponseDto getFullReport(String reportId);
}