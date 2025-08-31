package com.smooth.driving_analysis_service.reports.progress.dto;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ProgressResponseDto {
    private Long userId;
    private String cycleNo;            // 최신 사이클(없으면 null)
    private Integer totalTrips;        // 누적 주행 수
    private Integer currentCycleCount; // 현재 사이클 주행 수 (accumulator 기준)
    private Integer threshold;         // ex) 15
    private Integer remainingTrips;    // max(0, threshold - currentCycleCount)
}
