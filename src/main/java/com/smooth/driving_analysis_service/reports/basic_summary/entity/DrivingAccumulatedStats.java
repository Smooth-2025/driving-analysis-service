package com.smooth.driving_analysis_service.reports.basic_summary.entity;

import jakarta.persistence.*;
<<<<<<< HEAD
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * XADD 스트림 데이터와 DrivingRecord를 통합한 누적 통계 엔티티
 */
@Entity
@Table(name = "driving_accumulated_stats")
@Getter
=======
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
>>>>>>> origin/feat-us7.2
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrivingAccumulatedStats {
<<<<<<< HEAD
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "driving_id", nullable = false, unique = true)
    private String drivingId;
    
    // XADD 스트림 데이터
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
    
    // DrivingRecord 데이터
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
    
=======

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

>>>>>>> origin/feat-us7.2
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}