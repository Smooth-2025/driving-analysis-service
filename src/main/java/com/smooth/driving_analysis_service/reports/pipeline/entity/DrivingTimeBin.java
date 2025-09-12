package com.smooth.driving_analysis_service.reports.pipeline.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "driving_time_bin", indexes = {
        @Index(name = "idx_driving_time_bin_driving_id", columnList = "driving_id"),
        @Index(name = "idx_driving_time_bin_user_id", columnList = "user_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrivingTimeBin {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "driving_id", nullable = false, length = 100)
    private String drivingId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "hour_of_day", nullable = false)
    private Integer hourOfDay; // 0-23
    
    @Column(name = "minutes", nullable = false)
    private Integer minutes;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}