package com.smooth.driving_analysis_service.reports.accident_reaction.dto.request;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccidentReactionRenderedRequestDto {
    private long renderedAtMs;          // 프론트 그대로
    private String type;                // "accident-nearby" | "obstacle"
}
