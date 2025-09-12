package com.smooth.driving_analysis_service.reports.behavior.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "behavior_snapshot", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"report_fk", "status"}))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_fk", nullable = false)
    private Long reportFk; // MilestoneReport.id (PK)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    @Column(name = "payload_json", columnDefinition = "TEXT")
    private String payloadJson; // JSON 형태로 분석 결과 저장

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Status {
        INTERIM,  // 4, 8, 12회 중간 스냅샷
        FINAL     // 15회 최종 스냅샷
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}