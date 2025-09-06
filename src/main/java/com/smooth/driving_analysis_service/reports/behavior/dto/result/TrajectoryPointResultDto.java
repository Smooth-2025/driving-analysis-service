package com.smooth.driving_analysis_service.reports.behavior.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Athena 쿼리 결과 DTO (Task 2용)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrajectoryPointResultDto {
    private String behavior;    // "HARD_BRAKE", "RAPID_ACCEL", "LANE_CHANGE"
    private int dow;           // 요일 (1=월, 7=일)
    private String timeSlot;   // "DAWN", "COMMUTE", "DAY", "OFFWORK", "EVENING"
    private int cnt;           // 발생 횟수
}