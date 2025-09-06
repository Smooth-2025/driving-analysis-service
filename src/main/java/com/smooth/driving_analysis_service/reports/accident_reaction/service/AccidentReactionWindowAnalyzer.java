package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.Reaction;

public interface AccidentReactionWindowAnalyzer {
    
    /**
     * 알림 후 첫 번째 반응 이벤트를 찾아 분석
     * @param userId 사용자 ID
     * @param renderedAtMs 알림 렌더링 시각 (밀리초)
     * @param drivingId 주행 ID
     * @return 반응 분석 결과
     */
    Reaction findFirstReactionSessionBound(Long userId, long renderedAtMs, String drivingId);
}