package com.smooth.driving_analysis_service.reports.behavior.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Task 1: totalCounts 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorSummaryResponseDto {
    private int hardBrakeCount;
    private int rapidAccelCount;
    private int laneChangeCount;
    private int total;
}