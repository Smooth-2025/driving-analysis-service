// reports/accident_reaction/service/AccidentReactionService.java
package com.smooth.driving_analysis_service.reports.accident_reaction.service;
import java.util.Map;

public interface AccidentReactionService {
    record Ack(String drivingId) {}
    Ack recordAndAnalyzeAsync(String alertId, Long userId, long renderedAtMs, String type);
    Map<String,Object> summary(Long userId, String from, String to);
}
