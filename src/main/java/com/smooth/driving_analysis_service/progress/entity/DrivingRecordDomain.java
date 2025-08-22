// src/main/java/com/smooth/driving_analysis_service/progress/domain/TripSummary.java
package com.smooth.driving_analysis_service.progress.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trip_summary", indexes = {
        @Index(name = "idx_trip_user", columnList = "user_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DrivingRecordDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long key;   // AUTO_INCREMENT PK

    @Column(name = "trip_id", nullable = false)
    private String tripId;

    @Column(name = "user_id", nullable = false)
    private String userId;   // ← VARCHAR이니까 String

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "avg_speed")
    private Float avgSpeed;

    @Column(name = "cruise_ratio")
    private Float cruiseRatio;

    @Column(name = "lane_change_count")
    private Integer laneChangeCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
