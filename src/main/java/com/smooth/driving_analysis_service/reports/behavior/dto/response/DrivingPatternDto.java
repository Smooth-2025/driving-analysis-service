package com.smooth.driving_analysis_service.reports.behavior.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrivingPatternDto {
    private String weekday;       // 가장 위험행동이 많은 요일
    private String timeslot;      // 가장 위험행동이 많은 시간대
    private List<WeeklyChartDto> chart;  // 라인차트용
    private String comment;       // 패턴 분석 코멘트
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyChartDto {
        private String weekday;       // "월", "화", ...
        private ActionsDto actions;   // 행동별 시간대 정보
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionsDto {
        private ActionTimeSlotDto hardBrake;
        private ActionTimeSlotDto rapidAccel;
        private ActionTimeSlotDto laneChange;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionTimeSlotDto {
        private String timeSlot;  // "새벽", "출근", "낮", "퇴근", "저녁"
        private Integer count;    // 해당 시간대 발생 횟수
    }
}