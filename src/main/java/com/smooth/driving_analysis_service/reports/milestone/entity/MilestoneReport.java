package com.smooth.driving_analysis_service.reports.milestone.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "milestone_report", indexes = {
        @Index(name = "idx_milestone_report_user", columnList = "user_id"),
        @Index(name = "idx_milestone_report_status", columnList = "status"),
        @Index(name = "idx_milestone_report_user_cycle", columnList = "user_id, cycle_no")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneReport {

    public enum Status {
        COLLECTING,  // 주행 수집 중 (1~14회)
        PROCESSING,  // 15회 도달, 분석 중
        COMPLETED    // 분석 완료
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사용자 ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 사이클 번호 (1, 2, 3, ...) */
    @Column(name = "cycle_no", nullable = false)
    private Integer cycleNo;

    /** 리포트 ID (외부 노출용) */
    @Column(name = "report_id", nullable = false, unique = true, length = 50)
    private String reportId;

    /** 현재 주행 횟수 */
    @Builder.Default
    @Column(name = "number_of_driving", nullable = false)
    private Integer numberOfDriving = 0;

    /** 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    /** 읽음 여부 */
    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private Boolean read = false;
    
    public boolean isRead() {
        return read != null ? read : false;
    }

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static MilestoneReport newCollecting(Long userId, int cycleNo) {
        // INTERIM용 임시 reportId (실제로는 스케줄러에서 생성)
        String reportId = String.format("u%d_c%d_interim", userId, cycleNo);
        
        return MilestoneReport.builder()
                .userId(userId)
                .cycleNo(cycleNo)
                .reportId(reportId)
                .numberOfDriving(0)
                .status(Status.COLLECTING)
                .read(false)
                .build();
    }
    
    /**
     * FINAL 리포트 생성 (15회 도달 시)
     */
    public static MilestoneReport newFinal(Long userId, int cycleNo, String finalDate) {
        String reportId = String.format("u%d_c%d_final_%s", userId, cycleNo, finalDate);
        
        return MilestoneReport.builder()
                .userId(userId)
                .cycleNo(cycleNo)
                .reportId(reportId)
                .numberOfDriving(15)
                .status(Status.COMPLETED)
                .read(false)
                .build();
    }
}