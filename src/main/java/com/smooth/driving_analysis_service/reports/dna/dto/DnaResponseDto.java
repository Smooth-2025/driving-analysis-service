package com.smooth.driving_analysis_service.reports.dna.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DnaResponseDto {
    
    private String code;
    private String headline;
    private Map<String, Integer> radar;
    private List<AxisCard> axes;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AxisCard {
        private String id;
        private String label;
        private String summary;
    }
}