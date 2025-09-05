package com.smooth.driving_analysis_service.reports.basic_summary.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "driving_accumulated_stats",
        indexes = {
                @Index(name = "ix_das_user_id", columnList = "user_id"),
                @Index(name = "ix_das_driving_id", columnList = "driving_id"),
                @Index(name = "ix_das_created_at", columnList = "created_at")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrivingAccumulatedStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "driving_id", nullable = false, length = 255)
    private String drivingId;

    // XADD 필드들
    @Column(name = "driving_minutes")
    private Integer drivingMinutes;

    @Column(name = "total_distance")
    private Integer totalDistance;

    @Column(name = "lane_change_count")
    private Integer laneChangeCount;

    @Column(name = "hard_brake_count")
    private Integer hardBrakeCount;

    @Column(name = "rapid_accel_count")
    private Integer rapidAccelCount;

    // DrivingRecord 필드들
    @Column(name = "avg_speed")
    private Double avgSpeed;

    @Column(name = "cruise_ratio")
    private Double cruiseRatio;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}