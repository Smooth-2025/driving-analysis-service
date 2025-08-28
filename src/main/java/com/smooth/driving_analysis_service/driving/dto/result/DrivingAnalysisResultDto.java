package com.smooth.driving_analysis_service.driving.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrivingAnalysisResultDto {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double totalDistance;
    private Double avgSpeed;
    private Double maxSpeed;
    private Double minSpeed;
    private Double cruiseRatio;
}
