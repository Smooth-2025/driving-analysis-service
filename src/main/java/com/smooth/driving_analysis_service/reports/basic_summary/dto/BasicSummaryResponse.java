package com.smooth.driving_analysis_service.reports.basic_summary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasicSummaryResponse {
    
    private String reportId;
    
    private Double totalDistanceKm;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodStart;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodEnd;
    
    private Double averageDurationSec;
    
    private Double averageDistanceKm;
    
    private Double averageSpeedKmh;
    
    private Double averageCruiseRatio;
}