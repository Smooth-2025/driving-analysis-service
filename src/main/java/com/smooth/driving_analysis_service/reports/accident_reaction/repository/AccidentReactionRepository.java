// reports/accident_reaction/repository/AccidentReactionRepository.java
package com.smooth.driving_analysis_service.reports.accident_reaction.repository;
import java.time.LocalDateTime; import java.util.Map;

public interface AccidentReactionRepository {
    void upsertAlertRender(String alertId, Long userId, String drivingId, long renderedAtMs, String type);
    void upsertReactionMetric(String alertId, Long userId, String drivingId, long renderedAtMs,
                              boolean responded, Integer reactionMs, String eventType,
                              boolean decelOrStop, boolean evasiveManeuver);
    Map<String,Object> summary(Long userId, LocalDateTime from, LocalDateTime to);
}
