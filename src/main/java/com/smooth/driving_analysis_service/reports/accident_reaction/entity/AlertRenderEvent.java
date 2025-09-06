// reports/accident_reaction/entity/AlertRenderEvent.java
package com.smooth.driving_analysis_service.reports.accident_reaction.entity;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;

@Entity @Table(name="alert_render_event")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AlertRenderEvent {
    @Id @Column(length=64) private String alertId;
    private Long userId;
    @Column(length=64) private String drivingId;
    private LocalDateTime renderedAt;
    @Column(length=32) private String type;
    private LocalDateTime createdAt;
}
