package com.smooth.driving_analysis_service.reports.behavior.dto.result;

import com.smooth.driving_analysis_service.reports.behavior.entity.BehaviorType;
import com.smooth.driving_analysis_service.reports.behavior.entity.TimeSlot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class TrajectoryPointResultDto {
    private BehaviorType behavior;
    private int dayOfWeek;     // 1=월 ... 7=일
    private TimeSlot timeSlot; // 새벽/출근/낮/퇴근/저녁
    private int count;         // 해당 (요일×시간대)의 최다 횟수
}
