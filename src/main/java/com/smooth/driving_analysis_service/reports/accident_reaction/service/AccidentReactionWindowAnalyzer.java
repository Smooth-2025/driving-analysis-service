// reports/accident_reaction/service/AccidentReactionWindowAnalyzer.java
package com.smooth.driving_analysis_service.reports.accident_reaction.service;

public interface AccidentReactionWindowAnalyzer {
    record Reaction(boolean responded, Integer reactionMs, String eventType,
                    boolean decelOrStop, boolean evasiveManeuver) {}
    Reaction findFirstReactionSessionBound(Long userId, long renderedAtMs, String drivingId);
}
