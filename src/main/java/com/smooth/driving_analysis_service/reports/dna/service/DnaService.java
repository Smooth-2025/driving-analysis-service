package com.smooth.driving_analysis_service.reports.dna.service;

import com.smooth.driving_analysis_service.reports.dna.dto.response.DnaAnalysisResponseDto;

public interface DnaService {
    /**
     * 운전 성향 DNA 분석 결과 조회
     * @param reportId 리포트 ID (예: "u1_r3_20250901")
     * @return DNA 분석 결과
     */
    DnaAnalysisResponseDto getDnaAnalysis(String reportId);
}