package com.smooth.driving_analysis_service.reports.accident_reaction.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alert_render_event")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertRenderEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "alert_id", unique = true, nullable = false)
    private String alertId;
    
    @Column(name = "driving_id")
    private String drivingId;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "rendered_at_ms", nullable = false)
    private Long renderedAtMs;
    
    @Column(name = "alert_type")
    private String alertType;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}