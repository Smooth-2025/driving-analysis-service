package com.smooth.driving_analysis_service.driving.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
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
}
