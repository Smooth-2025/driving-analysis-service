package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import com.smooth.driving_analysis_service.reports.accident_reaction.dto.Ack;
import java.util.Map;

public interface AccidentReactionService {
    Map<String, Object> buildAccidentResponse(Long reportId);
    Ack recordAndAnalyzeAsync(String alertId, Long userId, long renderedAtMs, String type);
    Map<String, Object> summary(Long userId, String from, String to);
}