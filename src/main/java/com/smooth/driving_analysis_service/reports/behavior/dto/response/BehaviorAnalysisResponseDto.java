package com.smooth.driving_analysis_service.reports.behavior.dto.response;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BehaviorAnalysisResponseDto {
    
    private String reportId;
    private ActionSummary actionSummary;
    
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ActionSummary {
        private Map<String, Integer> totalCounts;    // 이번 리포트 총합
        private Map<String, Integer> prevCounts;     // 이전 리포트 총합  
        private Map<String, Double> diffPct;         // 증감 비율
        private List<WeeklyTimeSlot> weeklyTimeSlots; // 라인차트용
        private Map<String, String> patternSummary;   // A파트 멘트용
    }
    
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class WeeklyTimeSlot {
        private String weekday;                      // "월", "화", ...
        private Map<String, ActionTimeSlot> actions; // 행동별 시간대 정보
    }
    
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ActionTimeSlot {
        private String timeSlot;  // "새벽", "출근", "낮", "퇴근", "저녁"
        private Integer count;    // 해당 시간대 발생 횟수
    }
}