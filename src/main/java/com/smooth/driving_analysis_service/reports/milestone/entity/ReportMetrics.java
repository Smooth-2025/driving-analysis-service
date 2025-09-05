package com.smooth.driving_analysis_service.reports.milestone.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "report_metrics")
public class ReportMetrics {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="milestone_id", nullable=false)
    private Long milestoneId;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="total_minutes", nullable=false)
    private Integer totalMinutes;

    @Column(name="total_distance", nullable=false)
    private Integer totalDistance;

    @Column(name="trips", nullable=false)
    private Integer trips;

    @Column(name="created_at", nullable=false)
    private Instant createdAt;
}
