package com.smooth.driving_analysis_service.global.redis.dto;

import com.smooth.driving_analysis_service.driving.entity.DrivingRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrivingEventDto {

    private int v;
    private Long userId;
    private String drivingId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private int drivingMinutes;
    private int totalDistance;
    private int laneChangeCount;
    private int hardBrakeCount;
    private int rapidAccelCount;

    public static DrivingEventDto of(DrivingRecord drivingRecord){
        return DrivingEventDto.builder()
                .v(1)
                .userId(drivingRecord.getUserId())
                .drivingId(drivingRecord.getDrivingId())
                .startTime(drivingRecord.getStartTime())
                .endTime(drivingRecord.getEndTime())
                .status(drivingRecord.getStatus().name())
                .drivingMinutes((int) ChronoUnit.MINUTES.between(drivingRecord.getStartTime(), drivingRecord.getEndTime()))
                .totalDistance(Optional.ofNullable(drivingRecord.getTotalDistance())
                        .map(Double::intValue)
                        .orElse(0))
                .laneChangeCount(drivingRecord.getLaneChangeCount())
                .hardBrakeCount(drivingRecord.getHardBrakeCount())
                .rapidAccelCount(drivingRecord.getRapidAccelCount())
                .build();

    }
}