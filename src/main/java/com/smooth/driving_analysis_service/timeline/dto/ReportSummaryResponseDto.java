package com.smooth.driving_analysis_service.timeline.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportSummaryResponseDto {
    private Long id;
    private String reportId;
    private Integer numberOfDriving;
    private String status;
    private Boolean isRead;
}