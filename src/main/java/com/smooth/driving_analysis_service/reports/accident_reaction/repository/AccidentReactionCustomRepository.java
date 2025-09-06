package com.smooth.driving_analysis_service.reports.accident_reaction.repository;

import java.time.LocalDateTime;
import java.util.Map;

public interface AccidentReactionCustomRepository {
    
    void upsertAlertRender(String alertId, Long userId, String drivingId, LocalDateTime renderedAt, String type);
    
    void upsertReactionMetric(String alertId, Long userId, String drivingId, LocalDateTime renderedAt,
                             boolean responded, Integer reactionMs, String eventType,
                             boolean decelOrStop, boolean evasiveManeuver);
    
    Map<String, Object> getSummaryStats(Long userId, LocalDateTime from, LocalDateTime to);
    
    Double findAvgReactionMsByUserId(Long userId);
    
    Double findGlobalAvgReactionMs();
    
    Long countByUserId(Long userId);
    
    Map<String, Object> getUserReactionStats(Long userId);
    
    Long countDistinctUsersWithReaction();
    
    Long countDistinctUsersSlowerThan(int reactionTimeMs);
    
    Double getGlobalAverageReactionTime();
}