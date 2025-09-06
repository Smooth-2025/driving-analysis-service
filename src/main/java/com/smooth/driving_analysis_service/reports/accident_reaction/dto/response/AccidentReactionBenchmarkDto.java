package com.smooth.driving_analysis_service.reports.accident_reaction.dto.response;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccidentReactionBenchmarkDto {
    
    /**
     * 일반 평균 - 내 평균 (초)
     * 음수: 더 느림, 양수: 더 빠름
     */
    private Double deltaSec;
    
    /**
     * 차트 데이터
     */
    private ChartDto chart;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartDto {
        private String[] labels;      // ["일반 운전자", "내 주행"]
        private Double[] valuesSec;   // [일반 평균, 내 평균]
    }
}