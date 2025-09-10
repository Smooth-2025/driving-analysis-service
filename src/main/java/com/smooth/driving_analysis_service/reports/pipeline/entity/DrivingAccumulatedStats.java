package com.smooth.driving_analysis_service.reports.pipeline.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "driving_accumulated_stats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrivingAccumulatedStats {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "driving_id", nullable = false)
    private String drivingId;
    
    // XADD 필드들
    @Column(name = "driving_minutes", nullable = false)
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
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}