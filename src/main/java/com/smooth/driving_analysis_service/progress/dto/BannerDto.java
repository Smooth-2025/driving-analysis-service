package com.smooth.driving_analysis_service.progress.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class BannerDto {
    private String userId;
    private boolean showBanner;     // 배너 노출 여부 (reportsGenerated > ackedReports)
    private int reportsGenerated;   // 15회 단위로 생성된 리포트 개수
    private int ackedReports;       // 사용자가 '확인'한 리포트 개수
    private int pendingReports;     // 미확인 개수 (대개 0/1)
    private int threshold;          // 15 (참고용)
}
