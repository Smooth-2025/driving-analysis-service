package com.smooth.driving_analysis_service.reports.behavior.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompareDto {
    private Double incdec;        // 전체 증감률 (이번-이전)/이전 * 100
    private ChartDto chart;       // 바차트용 행동별 증감
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartDto {
        private BeforeAfterDto hardBrake;
        private BeforeAfterDto rapidAccel;
        private BeforeAfterDto laneChange;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BeforeAfterDto {
        private Integer before;   // 이전 리포트 값
        private Integer current;  // 현재 리포트 값
    }
}