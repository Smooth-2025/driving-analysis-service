package com.smooth.driving_analysis_service.reports.progress.dto;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ReadinessResponseDto {
    private Long userId;
    private String cycleNo;            // 최신 사이클(없으면 null)
    private Boolean ready;             // 15회 달성 여부
    private Integer missingTrips;      // 미달성 시 남은 횟수
    private Integer currentCycleCount; // 현재 사이클 주행 수
    private Integer totalTrips;        // 누적 주행 수
    private Integer threshold;         // ex) 15
}
