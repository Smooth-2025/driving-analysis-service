// reports/accident_reaction/entity/AccidentReactionMetric.java
package com.smooth.driving_analysis_service.reports.accident_reaction.entity;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;

@Entity @Table(name="accident_reaction_metric")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AccidentReactionMetric {
    @Id @Column(length=64) private String alertId;
    private Long userId;
    @Column(length=64) private String drivingId;
    private LocalDateTime renderedAt;

    private boolean reacted;     // 0/1
    private Integer reactionMs;  // null 가능
    @Column(length=32) private String eventType; // hard_brake/lane_change/sharp_turn/rapid_accel
    private boolean decelOrStop;
    private boolean evasiveManeuver;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
