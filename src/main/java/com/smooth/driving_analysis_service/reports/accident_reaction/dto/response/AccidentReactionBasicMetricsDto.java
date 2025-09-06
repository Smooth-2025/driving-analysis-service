package com.smooth.driving_analysis_service.reports.accident_reaction.dto.response;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccidentReactionBasicMetricsDto {
    private Integer receivedAlertCount;
    private Double avgReactionSec;
    private Double brakeOrStopRatio;
    private Double avoidRatio;
}