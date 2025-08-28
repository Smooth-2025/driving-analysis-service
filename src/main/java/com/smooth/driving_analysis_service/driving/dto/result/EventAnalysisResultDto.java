package com.smooth.driving_analysis_service.driving.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventAnalysisResultDto {
    private int laneChangeCount;
    private int hardBrakeCount;
    private int rapidAccelCount;
    private int sharpTurnCount;
}
