package com.smooth.driving_analysis_service.reports.accident_reaction.dto.response;

import lombok.Builder; import lombok.Getter;

@Getter @Builder
public class AccidentReactionSummaryResponseDto {
    private int totalAlerts;
    private int reactedAlerts;
    private double reactionRate;
    private double avgReactionMs;
    private double decelRate; //알림 이후 첫 반응 이벤트가 hard_brake인 비율
    private double evasiveRate; //알림 이후, 첫 반응 이벤트가 lane_chane, sharp_turn인 비율
}
