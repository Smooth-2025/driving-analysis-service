package com.smooth.driving_analysis_service.driving.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrivingCharacterAnalysisRequestDto {
    private double cruiseRatio;
    private double drivingMinutes;
    private double totalDistanceKm;
    private double avgSpeed;
    private double maxSpeed;
    private int laneChangeCount;
    private int rapidAccelCount;
    private int hardBrakeCount;
    private int sharpTurnCount;
    private int totalAggressiveCount;
}
