package com.smooth.driving_analysis_service.reports.accident_reaction.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "accident_reaction_metrics")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccidentReactionMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "driving_id", nullable = false)
    private String drivingId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "reaction_ms")
    private Long reactionMs;

    @Column(name = "responded")
    private Boolean responded;

    @Column(name = "decel_or_stop")
    private Boolean decelOrStop;

    @Column(name = "evasive_maneuver")
    private Boolean evasiveManeuver;

    @Column(name = "accident_type", length = 50)
    private String accidentType;

    @Column(name = "severity_level")
    private Integer severityLevel;

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
}