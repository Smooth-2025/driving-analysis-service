package com.smooth.driving_analysis_service.reports.basic_summary.service;

import com.smooth.driving_analysis_service.reports.basic_summary.dto.BasicSummaryResponse;

public interface BasicSummaryService {
    
    /**
<<<<<<< HEAD
     * 중간 리포트 생성/갱신 (INTERIM)
     */
    void generateInterimReport(Long reportId, Long userId);
    
    /**
     * 최종 리포트 생성 (FINAL)
     */
    void generateFinalReport(Long reportId, Long userId);
    
    /**
     * 리포트 상단 요약 조회
=======
     * 리포트 ID로 기본 통계 조회
>>>>>>> origin/feat-us7.2
     */
    BasicSummaryResponse getBasicSummary(Long reportId);
    
    /**
<<<<<<< HEAD
     * 중간 스냅샷 생성/갱신 (테스트용)
=======
     * INTERIM 스냅샷 생성/갱신
>>>>>>> origin/feat-us7.2
     */
    void createOrUpdateInterimSnapshot(Long reportId);
    
    /**
<<<<<<< HEAD
     * 최종 스냅샷 생성 (테스트용)
=======
     * FINAL 스냅샷 생성
>>>>>>> origin/feat-us7.2
     */
    void createFinalSnapshot(Long reportId);
}