package com.smooth.driving_analysis_service.reports.behavior.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompareDto {
    private Double incdec;
    private ChartDto chart;

    @Data
    @Builder
    public static class ChartDto {
        private BeforeAfterDto hardBrake;
        private BeforeAfterDto rapidAccel;
        private BeforeAfterDto laneChange;
    }

    @Data
    @Builder
    public static class BeforeAfterDto {
        private Integer before;
        private Integer current;
    }
}