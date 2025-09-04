package com.smooth.driving_analysis_service.timeline.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSummaryResponseDto {
    private Long id;
    private Boolean isRead;
    private String status;
}