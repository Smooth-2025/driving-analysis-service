// dto/DnaResponseDto.java
package com.smooth.driving_analysis_service.reports.dna.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DnaResponseDto {

    private String code;          // "A2-B1-C3-D4"
    private String headline;      // "적극적이며 빠른 반응형 운전자예요!"
    private Map<String, Integer> radar; // { "A":68, "B":55, "C":72, "D":60 }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AxisCard {
        private String id;        // "A" | "B" | "C" | "D"
        private String label;     // "출발 성향 (A2)"
        private String summary;   // 설명
    }

    private List<AxisCard> axes;
}
