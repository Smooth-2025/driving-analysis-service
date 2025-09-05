package com.smooth.driving_analysis_service.reports.behavior.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BehaviorTrajectoryResponseDto {
    private String insight; // A 멘트(패턴 요약)
    private List<DominantSlot> dominantSlots;

    @Data @Builder
    public static class DominantSlot {
        private String behavior;   // HARD_BRAKE, RAPID_ACCEL, LANE_CHANGE
        private int dayOfWeek;     // 1=월..7=일
        private String timeSlot;   // DAWN/COMMUTE_AM/DAY/COMMUTE_PM/EVENING
        private int count;         // 횟수(툴팁)
        private boolean hasData;   // 데이터 없는 요일이면 false
    }
}
