package com.smooth.driving_analysis_service.reports.basic_summary.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "basic_summary",
        indexes = {
                @Index(name = "idx_basic_summary_report", columnList = "report_id"),
                @Index(name = "idx_basic_summary_user", columnList = "user_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_basic_summary_report_type", 
                        columnNames = {"report_id", "snapshot_type"})
        }
)
public class BasicSummary {

    /**
     * 스냅샷 타입
     * INTERIM: 4/8/12회, 미노출, 교체 가능
     * FINAL: 15회, 노출용, 불변
     */
    public enum SnapshotType {
        INTERIM, FINAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK: milestone_report.id */
    @Column(name = "report_id", nullable = false)
    private Long reportId;

    /** 사용자 ID (조회 최적화용) */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 스냅샷 타입 */
    @Enumerated(EnumType.STRING)
    @Column(name = "snapshot_type", nullable = false, length = 10)
    private SnapshotType snapshotType;

    /** 총 주행 거리 (km) */
    @Column(name = "total_distance_km", precision = 10, scale = 2)
    private BigDecimal totalDistanceKm;

    /** 평균 주행 시간 (초) */
    @Column(name = "average_duration_sec", precision = 10, scale = 2)
    private BigDecimal averageDurationSec;

    /** 평균 주행 거리 (km) */
    @Column(name = "average_distance_km", precision = 10, scale = 2)
    private BigDecimal averageDistanceKm;

    /** 평균 속도 (km/h) */
    @Column(name = "average_speed_kmh", precision = 10, scale = 2)
    private BigDecimal averageSpeedKmh;

    /** 평균 크루즈 비율 */
    @Column(name = "average_cruise_ratio", precision = 5, scale = 4)
    private BigDecimal averageCruiseRatio;

    /** 기간 시작일 */
    @Column(name = "period_start")
    private LocalDate periodStart;

    /** 기간 종료일 */
    @Column(name = "period_end")
    private LocalDate periodEnd;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
=======
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
>>>>>>> origin/feat-us7.2
}