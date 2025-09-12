package com.smooth.driving_analysis_service.reports.basic_summary.entity;

import com.smooth.driving_analysis_service.reports.batch.dto.SnapshotType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "basic_summary_snapshot", uniqueConstraints = @UniqueConstraint(columnNames = { "report_id",
        "snapshot_type" }))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasicSummarySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_id", nullable = false)
    private Long reportId;

    @Enumerated(EnumType.STRING)
    @Column(name = "snapshot_type", nullable = false)
    private SnapshotType snapshotType;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Column(name = "user_id")
    private Long userId;

    // 기본 요약 데이터 필드들
    @Column(name = "total_distance_km")
    private Double totalDistanceKm;

    @Column(name = "total_driving_time_minutes")
    private Integer totalDrivingTimeMinutes;

    @Column(name = "average_speed_kmh")
    private Double averageSpeedKmh;

    @Column(name = "max_speed_kmh")
    private Double maxSpeedKmh;

    @Column(name = "driving_count")
    private Integer drivingCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
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
}