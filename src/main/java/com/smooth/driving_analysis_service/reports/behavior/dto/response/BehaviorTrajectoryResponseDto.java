package com.smooth.driving_analysis_service.reports.behavior.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Task 2: drivingPattern 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorTrajectoryResponseDto {
    private String weekday;
    private String timeslot;
    private List<DominantSlot> dominantSlots;
    private String comment;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DominantSlot {
        private String weekday;
        private Actions actions;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Actions {
        private TimeSlotCount hardBrake;
        private TimeSlotCount rapidAccel;
        private TimeSlotCount laneChange;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlotCount {
        private String timeSlot;
        private int count;
    }
}