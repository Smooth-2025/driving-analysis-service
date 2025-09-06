package com.smooth.driving_analysis_service.driving.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeeklyDrivingResponseDto {
    private int cruiseRatio;
    private int drivingMinutes;
    private double totalDistance;
    private int laneChangeCount;
    private int hardBrakeCount;
    private int rapidAccelCount;
    private int sharpTurnCount;
    private double avgSpeed;
}
