package com.smooth.driving_analysis_service.reports.basic_summary.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "basic_summary")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasicSummary {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "report_id", nullable = false)
    private Long reportId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "total_distance_km", nullable = false)
    private Double totalDistanceKm;
    
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;
    
    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;
    
    @Column(name = "average_duration_sec", nullable = false)
    private Double averageDurationSec;
    
    @Column(name = "average_distance_km", nullable = false)
    private Double averageDistanceKm;
    
    @Column(name = "average_speed_kmh", nullable = false)
    private Double averageSpeedKmh;
    
    @Column(name = "average_cruise_ratio", nullable = false)
    private Double averageCruiseRatio;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "snapshot_type", nullable = false)
    private SnapshotType snapshotType;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum SnapshotType {
        INTERIM, FINAL
    }
}