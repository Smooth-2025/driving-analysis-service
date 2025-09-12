package com.smooth.driving_analysis_service.reports.basic_summary.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasicSummaryResponseDto {
    
    private Long reportId;
    private Double totalDistanceKm;
    private Integer totalDrivingTimeMinutes;
    private Double averageSpeedKmh;
    private Double maxSpeedKmh;
    private Integer drivingCount;
}