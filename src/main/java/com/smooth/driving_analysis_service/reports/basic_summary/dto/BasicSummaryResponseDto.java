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
public class BasicSummaryResponseDto {
    
    private String reportId;
    
    private Double totalDistanceKm;           //15회 동안의 총 거리
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodStart;            //리포트 시작 날짜
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodEnd;              //리포트 마지막 날짜
    
    private Double averageDurationSec;        //평균 주행 시간
    
    private Double averageDistanceKm;         //평균 주행 거리

    private Double averageSpeedKmh;           //평균 속도
    
    private Double averageCruiseRatio;        //평균 정속중행률
}