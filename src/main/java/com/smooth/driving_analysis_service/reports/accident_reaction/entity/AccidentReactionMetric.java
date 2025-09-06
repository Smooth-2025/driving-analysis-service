package com.smooth.driving_analysis_service.reports.accident_reaction.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="accident_reaction_metric")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccidentReactionMetric {
    
    @Id
    @Column(length=64)
    private String alertId;
    
    private Long userId;
    
    @Column(length=64)
    private String drivingId;
    
    private LocalDateTime renderedAt;

    private boolean reacted;     // 0/1
    private Integer reactionMs;  // null 가능
    
    @Column(length=32)
    private String eventType; // hard_brake/lane_change/sharp_turn/rapid_accel
    
    private boolean decelOrStop;
    private boolean evasiveManeuver;

    @Column(name = "accident_type", length = 50)
    private String accidentType;

    @Column(name = "severity_level")
    private Integer severityLevel;

    @Column(name="window_s")
    private Integer windowSec;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // DNA 배치 서비스에서 사용하는 getter 메서드들
    public Boolean getResponded() { return reacted; }
    public Long getReactionMs() { return reactionMs != null ? reactionMs.longValue() : null; }
    public Boolean getDecelOrStop() { return decelOrStop; }
    public Boolean getEvasiveManeuver() { return evasiveManeuver; }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
