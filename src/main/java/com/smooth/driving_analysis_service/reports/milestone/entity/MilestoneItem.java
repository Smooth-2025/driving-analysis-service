package com.smooth.driving_analysis_service.reports.milestone.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;



@Getter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
public class MilestoneItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reportId;
    private String drivingId;
    private Integer orderNo;

    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
