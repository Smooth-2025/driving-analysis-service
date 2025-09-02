package com.smooth.driving_analysis_service.reports.behavior.dto.result;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorSummaryResultDto {
    private int hardBrakeCount;
    private int rapidAccelCount;
    private int laneChangeCount;
    private int total;
}
