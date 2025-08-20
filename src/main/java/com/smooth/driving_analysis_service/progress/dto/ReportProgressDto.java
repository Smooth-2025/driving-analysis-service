package com.smooth.driving_analysis_service.progress.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReportProgressDto {
    private long totalTrips;          // 누적 trip 수
    private int threshold;            // 다음 스냅샷 임계치 (15)
    private long remainingToThreshold;// 임계치까지 남은 개수 (0 미만 방지)
    private double progressRate;      // 임계치 대비 진행률 (0.0 ~ 1.0)
}
