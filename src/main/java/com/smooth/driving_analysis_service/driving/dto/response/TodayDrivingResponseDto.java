package com.smooth.driving_analysis_service.driving.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TodayDrivingResponseDto {
    private int cruiseRatio;
    private double totalDistance;
    private int drivingMinutes;
}
