package com.smooth.driving_analysis_service.reports.basic_summary.entity;

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
@Table(name = "driving_accumulated_stats")
public class DrivingAccumulatedStats {

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

    private Double cruiseRatio;

    private Integer drivingMinutes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}