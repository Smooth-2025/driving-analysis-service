package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import java.util.List;

/**
 * 사고 알림 반응 분석 배치 서비스
 */
public interface AccidentReactionBatchService {
    
    /**
     * 중간 리포트 생성 (4/8/12회)
     * @param reportId 리포트 ID
     * @param userId 사용자 ID
     * @param drivingIds 주행 ID 목록
     */
    void generateInterimReport(Long reportId, Long userId, List<String> drivingIds);
    
    /**
     * 최종 리포트 생성 (15회)
     * @param reportId 리포트 ID
     * @param userId 사용자 ID
     * @param drivingIds 주행 ID 목록
     */
    void generateFinalReport(Long reportId, Long userId, List<String> drivingIds);
}