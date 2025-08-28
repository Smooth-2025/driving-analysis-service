package com.smooth.driving_analysis_service.driving.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TodayDrivingResponseDto {
    int cruiseRatio;
    double totalDistance;
    int drivingMinutes;
}
