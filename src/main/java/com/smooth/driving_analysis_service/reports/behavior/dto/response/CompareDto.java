package com.smooth.driving_analysis_service.reports.behavior.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompareDto {
    private final Double incdec; // 증감률 (이번-이전)/이전 * 100
    private final String comment; // 비교 코멘트
    private final ChartDto chart; // 바차트용 데이터

    @Getter
    @Builder
    public static class ChartDto {
        private final BeforeAfterDto hardBrake;
        private final BeforeAfterDto rapidAccel;
        private final BeforeAfterDto laneChange;
    }

    @Getter
    @Builder
    public static class BeforeAfterDto {
        private final Integer before; // 이전 값
        private final Integer current; // 현재 값
    }
}