package com.smooth.driving_analysis_service.reports.dna.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DnaAnalysisResponseDto {
    
    private String reportId;
    private String headline;
    private RadarDto radar;
    private List<AxisDto> axes;
    
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RadarDto {
        @JsonProperty("A")
        private Integer A;  // 출발 성향 점수 (35/65/90)
        @JsonProperty("B")
        private Integer B;  // 감속 성향 점수
        @JsonProperty("C")
        private Integer C;  // 차선 변경 성향 점수
        @JsonProperty("D")
        private Integer D;  // 사고 대응 성향 점수
    }
    
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AxisDto {
        private String id;      // "A", "B", "C", "D"
        private String label;   // "A2", "B1", "C3", "D2"
        private String summary; // "일반형 출발: 무리하지 않는 적절한 가속이에요."
    }
}