// src/main/java/com/smooth/driving_analysis_service/reports/progress/entity/UserCycleAccumulator.java
package com.smooth.driving_analysis_service.reports.progress.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
@Table(name = "user_cycle_accumulator",
        indexes = {
                @Index(name = "idx_uca_user", columnList = "user_id"),
                @Index(name = "idx_uca_cycle", columnList = "cycle_no")
        })
public class UserCycleAccumulator {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private long userId;

    @Column(name = "cycle_no", nullable = false)
    private int cycleNo;

    @Column(name = "current_cycle_count", nullable = false)
    private int currentCycleCount;   // ← 필드명 꼭 currentCycleCount!

    @Column(name = "total_trips", nullable = false)
    private int totalTrips;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
