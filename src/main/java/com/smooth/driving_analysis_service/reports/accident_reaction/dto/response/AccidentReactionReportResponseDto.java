package com.smooth.driving_analysis_service.reports.accident_reaction.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccidentReactionReportResponseDto {
    
    private String reportId;                    // 리포트 ID
    private Integer receivedAlertCount;         // 수신한 알림 수
    private Double avgReactionSec;              // 평균 반응시간 (초)
    private Double brakeOrStopRatio;            // 급제동/정지 반응 비율
    private Double avoidRatio;                  // 회피 반응 비율
    private AccidentReactionBenchmarkDto benchmark; // 벤치마크 비교 데이터
}