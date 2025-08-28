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
    int cruiseRatio;
    int drivingMinutes;
    double totalDistance;
    int laneChangeCount;
    int hardBrakeCount;
    int rapidAccelCount;
    int sharpTurnCount;
    double avgSpeed;
}
