package com.smooth.driving_analysis_service.timeline.dto;


import lombok.*;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSummaryResponseDto {
    private Long id;
    private Boolean isRead;
    private String status;
}