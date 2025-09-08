package com.smooth.driving_analysis_service.reports.accident_reaction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Reaction {
    private final boolean responded;
    private final Long reactionMs;
    private final String eventType;
    private final boolean decelOrStop;
    private final boolean evasiveManeuver;
}