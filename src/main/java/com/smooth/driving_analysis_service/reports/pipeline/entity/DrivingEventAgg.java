package com.smooth.driving_analysis_service.reports.pipeline.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "driving_event_agg",
        indexes = { @Index(name="idx_dea_user", columnList="user_id") }
)
public class DrivingEventAgg {

    @Id
    @Column(name = "driving_id", length = 64)
    private String drivingId;          // PK: 주행 ID

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "lane_change_count", nullable = false)
    private int laneChangeCount;

    @Column(name = "hard_brake_count", nullable = false)
    private int hardBrakeCount;

    @Column(name = "rapid_accel_count", nullable = false)
    private int rapidAccelCount;

    @Column(name = "sharp_turn_count", nullable = false)
    private int sharpTurnCount;
}
