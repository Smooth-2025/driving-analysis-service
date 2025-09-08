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
    private Integer hardBrake;
    private Integer rapidAccel;
    private Integer laneChange;
    
    public static TotalCountsDto of(Integer hardBrake, Integer rapidAccel, Integer laneChange) {
        return new TotalCountsDto(hardBrake, rapidAccel, laneChange);
    }
    
    public Integer getTotal() {
        return hardBrake + rapidAccel + laneChange;
    }
}