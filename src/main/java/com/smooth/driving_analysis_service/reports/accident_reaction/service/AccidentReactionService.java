package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import java.util.Map;

public interface AccidentReactionService {
    
    /**
     * 중간 분석 스냅샷 생성/갱신 (4/8/12회)
     * @param reportId 리포트 ID
     */
    void createOrUpdateInterimSnapshot(Long reportId);
    
    /**
     * 최종 분석 스냅샷 생성 (15회)
     * @param reportId 리포트 ID
     */
    void createFinalSnapshot(Long reportId);
    
    /**
     * 사고 알림 렌더링 기록 및 분석
     * @param alertId 알림 ID
     * @param userId 사용자 ID
     * @param renderedAtMs 렌더링 시간 (밀리초)
     * @param type 알림 타입
     * @return 드라이빙 ID
     */
    String recordAndAnalyzeAsync(String alertId, Long userId, long renderedAtMs, String type);
    
    /**
     * 사고 반응 요약 조회
     * @param userId 사용자 ID
     * @param from 시작 날짜
     * @param to 종료 날짜
     * @return 요약 데이터
     */
    Map<String, Object> summary(Long userId, String from, String to);
}