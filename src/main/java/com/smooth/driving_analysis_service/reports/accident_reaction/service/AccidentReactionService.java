package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import com.smooth.driving_analysis_service.reports.accident_reaction.dto.request.AlertRenderRequestDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.AccidentReactionReportResponseDto;

public interface AccidentReactionService {
    
    /**
     * 알림 렌더링 이벤트 처리
     */
    void processAlertRender(String alertId, AlertRenderRequestDto request);
    
    /**
     * 사고 반응 리포트 조회
     */
    AccidentReactionReportResponseDto getAccidentReactionReport(String reportId);
}