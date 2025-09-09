package com.smooth.driving_analysis_service.reports.pipeline.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "driving_time_bin",
        indexes = {
                @Index(name="idx_dtb_driving", columnList="driving_id"),
                @Index(name="idx_dtb_user", columnList="user_id")
        }
)
public class DrivingTimeBin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                   // auto inc

    @Column(name = "driving_id", length = 64, nullable = false)
    private String drivingId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "hour_of_day", nullable = false) // 0~23
    private int hourOfDay;

    @Column(name = "minutes", nullable = false)     // 해당 시(hour)에 머문 분
    private int minutes;
}
