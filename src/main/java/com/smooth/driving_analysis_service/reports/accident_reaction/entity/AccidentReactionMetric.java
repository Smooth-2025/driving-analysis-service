package com.smooth.driving_analysis_service.reports.accident_reaction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "accident_reaction_metric",
        uniqueConstraints = @UniqueConstraint(name="uk_metric_alert", columnNames = {"alert_id"}),
        indexes = @Index(name="ix_metric_user", columnList="user_id"))
public class AccidentReactionMetric {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="alert_id", nullable=false, length=64)
    private String alertId;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="reaction_ms")
    private Integer reactionMs;

    @Column(name="responded", nullable=false)
    private boolean responded;

    @Column(name="decel_or_stop", nullable=false)
    private boolean decelOrStop;

    @Column(name="evasive_maneuver", nullable=false)
    private boolean evasiveManeuver;

    @Column(name="window_s", nullable=false)
    private int windowSec;

    @CreationTimestamp
    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;
}
