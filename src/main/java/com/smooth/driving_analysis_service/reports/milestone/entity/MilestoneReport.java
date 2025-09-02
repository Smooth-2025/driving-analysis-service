//package com.smooth.driving_analysis_service.reports.milestone.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import java.time.LocalDateTime;
//
//@Getter
//@Setter
//@Builder
//@AllArgsConstructor
//@NoArgsConstructor
//@Entity
//@Table(
//        name = "milestone_report",
//        uniqueConstraints = {
//                @UniqueConstraint(
//                        name = "uk_milestone_report_user_cycle",
//                        columnNames = {"user_id", "cycle_no"}
//                )
//        },
//        indexes = {
//                @Index(name = "idx_milestone_report_user", columnList = "user_id"),
//                @Index(name = "idx_milestone_report_status", columnList = "status")
//        }
//)
//public class MilestoneReport {
//
//    /**
//     * 상태 정의
//     * COLLECTING : 로그 수집 중
//     * PROCESSING : 15개 도달 후 분석 중
//     * COMPLETED  : 분석 완료 (타임라인 READY 상태와 매핑)
//     */
//    public enum Status {
//        COLLECTING,
//        PROCESSING,
//        COMPLETED
//    }
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "user_id", nullable = false)
//    private Long userId;
//
//    /**
//     * 1,2,3... 사용자별 사이클 번호
//     * (userId, cycleNo) 유니크 보장
//     */
//    @Column(name = "cycle_no", nullable = false)
//    private Integer cycleNo;
//
//    /**
//     * 누적된 총 주행 수 (선택적으로 채움)
//     */
//    @Column(name = "total_trips")
//    private Integer totalTrips;
//
//    /**
//     * 현재 리포트 상태
//     */
//    @Enumerated(EnumType.STRING)
//    @Column(name = "status", nullable = false, length = 20)
//    @Builder.Default
//    private Status status = Status.COLLECTING;
//
//    /**
//     * 읽음 여부 (false 기본값)
//     */
//    @Column(name = "read_flag", nullable = false)
//    @Builder.Default
//    private boolean read = false;
//
//    @CreationTimestamp
//    @Column(name = "created_at", updatable = false)
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
//}
package com.smooth.driving_analysis_service.reports.milestone.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "milestone_report",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_milestone_report_user_cycle", columnNames = {"user_id", "cycle_no"}),
                @UniqueConstraint(name = "uk_milestone_report_code", columnNames = {"report_id"})
        },
        indexes = {
                @Index(name = "idx_milestone_report_user", columnList = "user_id"),
                @Index(name = "idx_milestone_report_status", columnList = "status")
        }
)
public class MilestoneReport {

    /** 리포트 상태 */
    public enum Status {
        COLLECTING,   // 주행 스탬프 수집 중
        PROCESSING,   // 15개 채워져 분석/집계 처리 중
        COMPLETED     // 분석 완료
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사람이 읽기 쉬운 문자열 ID (예: u1_r3_20250901) */
    @Column(name = "report_id", nullable = false, length = 48)
    private String reportId;

    /** 유저 식별자 */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 유저별 N번째 리포트 번호 (표시용 reportNo) */
    @Column(name = "cycle_no", nullable = false)
    private Integer cycleNo;

    /** 해당 리포트(사이클)에 포함된 주행 누적 갯수 (0~15) */
    @Column(name = "number_of_driving", nullable = false)
    @Builder.Default
    private Integer numberOfDriving = 0;

    /** 리포트 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.COLLECTING;

    /** 읽음 여부 */
    @Column(name = "read_flag", nullable = false)
    @Builder.Default
    private boolean read = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** 영속 직전 reportId 자동 생성 (비어있을 때만) */
    @PrePersist
    void prePersist() {
        if (this.reportId == null || this.reportId.isBlank()) {
            // 서울 시간 기준 yyyyMMdd
            String ymd = LocalDate.now(ZoneId.of("Asia/Seoul"))
                    .format(DateTimeFormatter.BASIC_ISO_DATE); // ex) 20250901
            this.reportId = String.format("u%d_r%d_%s", this.userId, this.cycleNo, ymd);
        }
    }

    /** 표시용 reportNo == cycleNo */
    @Transient
    public int getReportNo() {
        return this.cycleNo == null ? 0 : this.cycleNo;
    }

    /** UI 라벨: report(N) */
    @Transient
    public String getLabel() {
        return "report(" + getReportNo() + ")";
    }

    /** 새 사이클 스켈레톤 생성 편의 메서드 */
    public static MilestoneReport newCollecting(Long userId, int cycleNo) {
        return MilestoneReport.builder()
                .userId(userId)
                .cycleNo(cycleNo)
                .numberOfDriving(0)
                .status(Status.COLLECTING)
                .read(false)
                .build();
    }
}
