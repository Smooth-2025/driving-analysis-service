package com.smooth.driving_analysis_service.reports.accident_reaction.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccidentReactionBenchmarkDto {
    
    /**
     * 일반 평균 - 내 평균 (초)
     * 음수: 더 느림, 양수: 더 빠름
     */
    private final Double deltaSec;
    
    /**
     * 차트 데이터
     */
    private final ChartDto chart;
    
    @Getter
    @Builder
    public static class ChartDto {
        private final String[] labels;      // ["일반 운전자", "내 주행"]
        private final Double[] valuesSec;   // [일반 평균, 내 평균]
    }
}