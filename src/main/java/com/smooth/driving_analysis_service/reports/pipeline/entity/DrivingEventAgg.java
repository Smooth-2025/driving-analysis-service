package com.smooth.driving_analysis_service.reports.pipeline.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "driving_event_agg")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrivingEventAgg {
    
    @Id
    @Column(name = "driving_id", length = 100)
    private String drivingId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Builder.Default
    @Column(name = "lane_change_count", nullable = false)
    private Integer laneChangeCount = 0;
    
    @Builder.Default
    @Column(name = "hard_brake_count", nullable = false)
    private Integer hardBrakeCount = 0;
    
    @Builder.Default
    @Column(name = "rapid_accel_count", nullable = false)
    private Integer rapidAccelCount = 0;
    
    @Builder.Default
    @Column(name = "sharp_turn_count", nullable = false)
    private Integer sharpTurnCount = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}