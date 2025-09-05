package com.smooth.driving_analysis_service.reports.accident_reaction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "alert_render_event",
        uniqueConstraints = @UniqueConstraint(name="uk_render_unique", columnNames = {"alert_id"}),
        indexes = {
                @Index(name="ix_render_user_time", columnList="user_id,rendered_at"),
                @Index(name="ix_render_driving", columnList="driving_id")
        })
public class AlertRenderEvent {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="alert_id", nullable=false, length=64)
    private String alertId;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="type", nullable=false, length=32)
    private String type; // "accident-nearby" | "obstacle"

    @Column(name="rendered_at", nullable=false)
    private LocalDateTime renderedAt;

    @CreationTimestamp
    @Column(name="received_at", nullable=false)
    private LocalDateTime receivedAt;

    @Column(name="driving_id", length=64)
    private String drivingId; // 서버에서 자동 추적
}
