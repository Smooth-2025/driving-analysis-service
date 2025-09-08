package com.smooth.driving_analysis_service.reports.accident_reaction.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alert_render_event")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertRenderEvent {

    @Id
    @Column(name = "alert_id")
    private String alertId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "type", nullable = false)
    private String type; // "accident-nearby" | "obstacle"

    @Column(name = "rendered_at", nullable = false)
    private LocalDateTime renderedAt;

    @Column(name = "driving_id")
    private String drivingId; // nullable, 자동 추적 결과

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    @PrePersist
    protected void onCreate() {
        if (receivedAt == null) {
            receivedAt = LocalDateTime.now();
        }
    }
}