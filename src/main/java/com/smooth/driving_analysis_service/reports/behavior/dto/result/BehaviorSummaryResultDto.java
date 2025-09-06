package com.smooth.driving_analysis_service.reports.behavior.dto.result;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Task 1: totalCounts 결과 DTO
 */
@Data
@Builder
@NoArgsConstructor
public class BehaviorSummaryResultDto {
    private int hardBrakeCount;
    private int rapidAccelCount;
    private int laneChangeCount;
    
    public BehaviorSummaryResultDto(int hardBrakeCount, int rapidAccelCount, int laneChangeCount) {
        this.hardBrakeCount = hardBrakeCount;
        this.rapidAccelCount = rapidAccelCount;
        this.laneChangeCount = laneChangeCount;
    }
    
    public int getTotal() {
        return hardBrakeCount + rapidAccelCount + laneChangeCount;
    }
}