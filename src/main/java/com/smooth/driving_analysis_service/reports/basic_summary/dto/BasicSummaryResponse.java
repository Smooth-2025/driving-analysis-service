package com.smooth.driving_analysis_service.reports.basic_summary.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class BasicSummaryResponse {
    
    /** 리포트 ID */
    private String reportId;
    
    /** 총 주행 거리 (km) */
    private BigDecimal totalDistanceKm;
    
    /** 기간 시작일 */
    private LocalDate periodStart;
    
    /** 기간 종료일 */
    private LocalDate periodEnd;
    
    /** 평균 주행 시간 (초) */
    private BigDecimal averageDurationSec;
    
    /** 평균 주행 거리 (km) */
    private BigDecimal averageDistanceKm;
    
    /** 평균 속도 (km/h) */
    private BigDecimal averageSpeedKmh;
    
    /** 평균 크루즈 비율 */
    private BigDecimal averageCruiseRatio;
}