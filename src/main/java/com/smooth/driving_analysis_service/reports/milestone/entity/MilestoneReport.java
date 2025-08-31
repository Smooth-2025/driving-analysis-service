package com.smooth.driving_analysis_service.reports.milestone.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "milestone_report",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_milestone_report_user_cycle",
                        columnNames = {"user_id", "cycle_no"}
                )
        },
        indexes = {
                @Index(name = "idx_milestone_report_user", columnList = "user_id"),
                @Index(name = "idx_milestone_report_status", columnList = "status")
        }
)
public class MilestoneReport {

    /**
     * 상태 정의
     * COLLECTING : 로그 수집 중
     * PROCESSING : 15개 도달 후 분석 중
     * COMPLETED  : 분석 완료 (타임라인 READY 상태와 매핑)
     */
    public enum Status {
        COLLECTING,
        PROCESSING,
        COMPLETED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 1,2,3... 사용자별 사이클 번호
     * (userId, cycleNo) 유니크 보장
     */
    @Column(name = "cycle_no", nullable = false)
    private Integer cycleNo;

    /**
     * 누적된 총 주행 수 (선택적으로 채움)
     */
    @Column(name = "total_trips")
    private Integer totalTrips;

    /**
     * 현재 리포트 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.COLLECTING;

    /**
     * 읽음 여부 (false 기본값)
     */
    @Column(name = "read_flag", nullable = false)
    @Builder.Default
    private boolean read = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
