package com.smooth.driving_analysis_service.reports.accident_reaction.dto.response;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccidentReactionResponseDto {
    private String alertId;
    private long userId;
    private String drivingId;        // 서버에서 자동 추적된 값
    private long serverReceivedAtMs;
    private boolean analysisScheduled;
}
