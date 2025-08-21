package com.smooth.driving_analysis_service.progress.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class EligibilityDto {
    private String userId;
    private boolean eligible;       // 15회 달성 여부
    private int missingTrips;       // 부족 개수(달성 시 0)
    private int currentCycleCount;  // 이번 사이클 누적(0~15, 배수면 15)
    private int totalTrips;         // 누적 주행 수
    private int threshold;          // 15
}
