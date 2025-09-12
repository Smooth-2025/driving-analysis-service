package com.smooth.driving_analysis_service.reports.batch.service;

/**
 * 사이클 기반 리포트 배치 서비스
 */
public interface CycleReportBatchService {
    
    /**
     * 모든 사용자의 사이클 리포트 처리
     * @param dateStr 날짜 문자열 (yyyyMMdd)
     */
    void processAllUsers(String dateStr);
    
    /**
     * 특정 사용자의 사이클 리포트 처리
     * @param userId 사용자 ID
     * @param dateStr 날짜 문자열 (yyyyMMdd)
     */
    void processUser(Long userId, String dateStr);
}