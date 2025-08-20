package com.smooth.driving_analysis_service.progress.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportEligibilityDto {
    private long totalTrips;     // 누적 주행 수
    private int threshold;       // 기준(15)
    private boolean isEligible;  // 15회 달성 여부
    private long remainingTrips; // 부족한 개수(0 미만 방지)
}
