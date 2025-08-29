package com.smooth.driving_analysis_service.driving.dto.response;

import com.smooth.driving_analysis_service.driving.entity.DrivingRecord;
import com.smooth.driving_analysis_service.driving.entity.SummaryStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrivingRecordResponseDto {

    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double totalDistance;
    private Double avgSpeed;
    private Double cruiseRatio;
    private int laneChangeCount;
    private int hardBrakeCount;
    private int rapidAccelCount;
    private int sharpTurnCount;
    @Enumerated(EnumType.STRING)
    private SummaryStatus status;

    public static DrivingRecordResponseDto from(DrivingRecord drivingRecord){
        return DrivingRecordResponseDto.builder()
                .id(drivingRecord.getId())
                .startTime(drivingRecord.getStartTime())
                .endTime(drivingRecord.getEndTime())
                .totalDistance(Math.round(drivingRecord.getTotalDistance() / 1000.0 * 10.0) / 10.0)
                .avgSpeed(Math.round(drivingRecord.getAvgSpeed() * 10.0) / 10.0)
                .cruiseRatio(Math.round(drivingRecord.getCruiseRatio() * 1000.0) / 10.0)
                .laneChangeCount(drivingRecord.getLaneChangeCount())
                .hardBrakeCount(drivingRecord.getHardBrakeCount())
                .rapidAccelCount(drivingRecord.getRapidAccelCount())
                .sharpTurnCount(drivingRecord.getSharpTurnCount())
                .status(drivingRecord.getStatus())
                .build();
    }

}
