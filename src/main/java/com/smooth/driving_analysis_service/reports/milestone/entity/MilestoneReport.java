package com.smooth.driving_analysis_service.reports.milestone.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
public class MilestoneReport {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Integer cycleNo;      // 15회 단위

    private Integer totalTrips;   // 스냅샷 시점 누적
    private Boolean isRead;

    private LocalDateTime snapshotAt;
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (snapshotAt == null) snapshotAt = LocalDateTime.now();
        if (isRead == null) isRead = false;
    }

    public void setRead(boolean read) {
        this.isRead = read;
    }
}
