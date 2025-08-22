package com.smooth.driving_analysis_service.snapshot.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cycle_accumulator")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CycleAccumulatorEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "cycle_index", nullable = false)
    private int cycleIndex; // 기본 1

    @Column(name = "trip_count", nullable = false)
    private int tripCount;  // 0..15

    @Column(name = "first_trip_id")
    private String firstTripId;

    @Column(name = "last_trip_id")
    private String lastTripId;

    @Column(name = "rapid_sum", nullable = false)
    private int rapidSum;

    @Column(name = "hard_sum", nullable = false)
    private int hardSum;

    @Column(name = "lane_sum", nullable = false)
    private int laneSum;

    @Column(name = "turn_sum", nullable = false)
    private int turnSum;

    @Column(name = "distance_m", nullable = false)
    private long distanceM;

    @Column(name = "duration_s", nullable = false)
    private long durationS;

    public static CycleAccumulatorEntity newEmpty(Long userId) {
        return CycleAccumulatorEntity.builder()
                .userId(userId)
                .cycleIndex(1)
                .tripCount(0)
                .rapidSum(0).hardSum(0).laneSum(0).turnSum(0)
                .distanceM(0L).durationS(0L)
                .build();
    }
}
