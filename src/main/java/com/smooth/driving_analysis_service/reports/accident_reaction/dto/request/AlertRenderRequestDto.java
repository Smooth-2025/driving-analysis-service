package com.smooth.driving_analysis_service.reports.accident_reaction.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlertRenderRequestDto {
    private Long renderedAtMs;  // 화면에 띄워진 알림 시간 (밀리초)
    private String type;        // 알림 타입 (예: "accident-nearby")
}