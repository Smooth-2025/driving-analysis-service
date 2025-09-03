package com.smooth.driving_analysis_service.reports.behavior.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class BehaviorCommentResponseDto {
    private String text; // 한 줄 멘트
}
