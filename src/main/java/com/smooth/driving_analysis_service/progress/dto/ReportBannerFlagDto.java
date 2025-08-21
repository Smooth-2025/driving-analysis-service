package com.smooth.driving_analysis_service.progress.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReportBannerFlagDto {
    private boolean eligible;   // 달성 여부(배너 노출 기준)
    private boolean bannerAck;  // 배너 확인(닫힘) 여부
}
