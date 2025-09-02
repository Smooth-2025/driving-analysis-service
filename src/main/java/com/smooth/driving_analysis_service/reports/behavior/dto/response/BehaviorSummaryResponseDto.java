package com.smooth.driving_analysis_service.reports.behavior.dto.response;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorSummaryResponseDto {
    private int hardBrakeCount;
    private int rapidAccelCount;
    private int laneChangeCount;
    private int total;
    }
