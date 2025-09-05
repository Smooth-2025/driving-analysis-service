package com.smooth.driving_analysis_service.reports.behavior.dto.request;

import lombok.Data;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BehaviorDiffRequestDto {
    private Long prevReportId; // 비교할 이전 리포트 ID
}
