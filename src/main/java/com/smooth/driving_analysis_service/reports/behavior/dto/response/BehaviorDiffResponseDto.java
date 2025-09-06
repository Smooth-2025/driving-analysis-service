package com.smooth.driving_analysis_service.reports.behavior.dto.response;

import com.smooth.driving_analysis_service.reports.behavior.dto.result.BehaviorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Task 3: compare 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorDiffResponseDto {
    private String type; // "hardBrake", "rapidAccel", "laneChange"
    private BehaviorType behavior;
    private int before;
    private int current;
    private double changePercent;
    private Direction direction;
    
    public enum Direction {
        INCREASE, DECREASE, FLAT
    }
}