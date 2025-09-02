package com.smooth.driving_analysis_service.reports.accident_reaction.dto.result;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccidentReactionResultDto {
    private int alertsReceived;
    private Long avgResponseMs;
    private double brakeOrStopRatio;
    private double evasiveRatio;
}
