package com.smooth.driving_analysis_service.progress.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProgressDto {
    private String userId;
    private long totalTrips;          // 누적 trip 수
    private int threshold;            // 다음 스냅샷 임계치 (15)
    private long remainingTrips;// 임계치까지 남은 개수 (0 미만 방지)
    private int currentCycleCount;   // 0~15 (배수이면서 >0이면 15)
    private int reportsGenerated;  //15회 누적 리포트 생성 개수 (totalTrips/threshold)
}
