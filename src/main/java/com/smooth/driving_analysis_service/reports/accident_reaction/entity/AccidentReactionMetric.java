package com.smooth.driving_analysis_service.reports.accident_reaction.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "accident_reaction_metric")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccidentReactionMetric {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "alert_id", nullable = false)
    private String alertId;
    
    @Column(name = "driving_id", nullable = false)
    private String drivingId;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "reaction_ms")
    private Long reactionMs;  // 반응 시간 (밀리초)
    
    @Column(name = "reacted")
    private Boolean reacted;  // 반응 여부
    
    @Column(name = "reaction_type")
    private String reactionType;  // hard_brake, lane_change, sharp_turn
    
    @Column(name = "decel_or_stop")
    private Boolean decelOrStop;  // 감속/정지 여부
    
    @Column(name = "evasive_maneuver")
    private Boolean evasiveManeuver;  // 우회 기동 여부
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}