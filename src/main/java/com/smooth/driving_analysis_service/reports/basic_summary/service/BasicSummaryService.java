package com.smooth.driving_analysis_service.reports.basic_summary.service;

import com.smooth.driving_analysis_service.reports.basic_summary.dto.response.BasicSummaryResponseDto;

public interface BasicSummaryService {
    
    /**
     * 리포트 ID로 기본 요약 정보 조회
     * @param reportId 리포트 ID (새로운 형식 지원: u123_c3_interim, u123_c3_final_20250912)
     * @param userId 사용자 ID
     * @return 기본 요약 응답 DTO
     */
    BasicSummaryResponseDto getBasicSummaryByReportId(String reportId, Long userId);
}