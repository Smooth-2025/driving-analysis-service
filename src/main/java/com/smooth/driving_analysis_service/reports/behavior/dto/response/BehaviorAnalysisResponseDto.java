package com.smooth.driving_analysis_service.reports.behavior.dto.response;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BehaviorAnalysisResponseDto {
    
    private String reportId;
    private TotalCounts totalCounts;
    private DrivingPattern drivingPattern;  // Task 2에서 구현
    private Compare compare;                // Task 3에서 구현
    
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TotalCounts {
        private Integer hardBrake;    // 급제동
        private Integer rapidAccel;   // 급가속
        private Integer laneChange;   // 차선변경
        private Integer total;        // 총합
    }
    
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DrivingPattern {
        private String weekday;       // "금요일"
        private String timeslot;      // "저녁", "퇴근", "낮", "출근", "새벽"
        private List<WeeklyChart> chart;  // 라인차트용
        private String comment;       // 패턴 분석 코멘트
    }
    
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class WeeklyChart {
        private String weekday;       // "월", "화", ...
        private Actions actions;      // 행동별 시간대 정보
    }
    
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Actions {
        private ActionTimeSlot hardBrake;
        private ActionTimeSlot rapidAccel;
        private ActionTimeSlot laneChange;
    }
    
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ActionTimeSlot {
        private String timeSlot;  // "새벽", "출근", "낮", "퇴근", "저녁"
        private Integer count;    // 해당 시간대 발생 횟수
    }
    
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Compare {
        private Double incdec;        // 증감률 (이번-이전)/이전 * 100
        private Chart chart;          // 바차트용
    }
    
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Chart {
        private BeforeAfter hardBrake;
        private BeforeAfter rapidAccel;
        private BeforeAfter laneChange;
    }
    
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class BeforeAfter {
        private Integer before;   // 이전
        private Integer current;  // 현재
    }
}