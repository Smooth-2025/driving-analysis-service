package com.smooth.driving_analysis_service.reports.behavior.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Task 1: totalCounts DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TotalCountsDto {
    private int hardBrake;
    private int rapidAccel;
    private int laneChange;
    
    public static TotalCountsDto of(int hardBrake, int rapidAccel, int laneChange) {
        return new TotalCountsDto(hardBrake, rapidAccel, laneChange);
    }
    
    public int getTotal() {
        return hardBrake + rapidAccel + laneChange;
    }
}