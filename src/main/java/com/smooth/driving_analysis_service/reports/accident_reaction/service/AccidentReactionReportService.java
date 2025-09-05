package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.AccidentReactionBasicMetricsDto;
<<<<<<< HEAD
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.AccidentReactionBenchmarkDto;
=======
>>>>>>> 437e3459a657278af0bed63445b86a1591a7cc39
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.AccidentReactionReportResponseDto;

public interface AccidentReactionReportService {
    
    /**
     * Task 1: 기본 반응 지표 조회
     * @param reportId 마일스톤 리포트 ID
     * @return 기본 반응 지표
     */
    AccidentReactionBasicMetricsDto getBasicMetrics(String reportId);
    
    /**
<<<<<<< HEAD
     * Task 2: 벤치마크 비교 데이터 조회
     * @param reportId 마일스톤 리포트 ID
     * @return 벤치마크 비교 데이터
     */
    AccidentReactionBenchmarkDto getBenchmark(String reportId);
    
    /**
=======
>>>>>>> 437e3459a657278af0bed63445b86a1591a7cc39
     * 전체 리포트 응답 생성 (Task 1 + Task 2)
     * @param reportId 마일스톤 리포트 ID
     * @return 완전한 리포트 응답
     */
    AccidentReactionReportResponseDto getFullReport(String reportId);
}