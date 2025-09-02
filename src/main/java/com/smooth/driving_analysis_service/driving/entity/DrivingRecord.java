package com.smooth.driving_analysis_service.driving.entity;

import com.smooth.driving_analysis_service.driving.dto.result.DrivingAnalysisResultDto;
import com.smooth.driving_analysis_service.driving.dto.result.EventAnalysisResultDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class DrivingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String drivingId;

    private Long userId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Double totalDistance;

    private Double avgSpeed;

    private Double maxSpeed;

    private Double minSpeed;

    private Double cruiseRatio;

    private int laneChangeCount;

    private int hardBrakeCount;

    private int rapidAccelCount;

    private int sharpTurnCount;

    @Enumerated(EnumType.STRING)
    private SummaryStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void update(DrivingAnalysisResultDto drivingResult, EventAnalysisResultDto eventResult) {
        this.startTime = drivingResult.getStartTime();
        this.endTime = drivingResult.getEndTime();
        this.totalDistance = drivingResult.getTotalDistance();
        this.avgSpeed = drivingResult.getAvgSpeed();
        this.maxSpeed = drivingResult.getMaxSpeed();
        this.minSpeed = drivingResult.getMinSpeed();
        this.cruiseRatio = drivingResult.getCruiseRatio();
        this.laneChangeCount = eventResult.getLaneChangeCount();
        this.hardBrakeCount = eventResult.getHardBrakeCount();
        this.rapidAccelCount = eventResult.getRapidAccelCount();
        this.sharpTurnCount = eventResult.getSharpTurnCount();
        this.status = SummaryStatus.COMPLETED;
    }

    public static DrivingRecord createInitialRecord(String drivingId, Long userId) {
        return DrivingRecord.builder()
                .drivingId(drivingId)
                .userId(userId)
                .status(SummaryStatus.PROCESSING)
                .totalDistance(0.0)
                .avgSpeed(0.0)
                .maxSpeed(0.0)
                .minSpeed(0.0)
                .cruiseRatio(0.0)
                .laneChangeCount(0)
                .hardBrakeCount(0)
                .rapidAccelCount(0)
                .sharpTurnCount(0)
                .build();
    }

}
