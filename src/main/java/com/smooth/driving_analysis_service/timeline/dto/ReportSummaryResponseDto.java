package com.smooth.driving_analysis_service.timeline.dto;


import lombok.*;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSummaryResponseDto {
    private Long id; //리포트 pk
    private boolean isRead; //읽음 여부
}