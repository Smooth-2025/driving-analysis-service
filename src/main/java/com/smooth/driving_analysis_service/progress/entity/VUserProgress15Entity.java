package com.smooth.driving_analysis_service.progress.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "v_user_progress_15")
@Immutable // 읽기 전용
@Getter
public class VUserProgress15Entity {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @Column(name = "total_trips")
    private Integer totalTrips;

    @Column(name = "threshold")
    private Integer threshold;

    @Column(name = "remaining_trips")
    private Integer remainingTrips;

    @Column(name = "current_cycle_count")
    private Integer currentCycleCount;

    @Column(name = "reports_generated")
    private Integer reportsGenerated;

    protected VUserProgress15Entity() {}
}
