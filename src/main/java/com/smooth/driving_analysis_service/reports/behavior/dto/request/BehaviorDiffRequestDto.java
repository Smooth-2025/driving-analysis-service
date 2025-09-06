package com.smooth.driving_analysis_service.reports.behavior.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Task 3: compare 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorDiffRequestDto {
    private String type; // "all" or specific behavior type
    private Long prevReportId; // 이전 리포트 ID
}