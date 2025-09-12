package com.smooth.driving_analysis_service.reports.milestone.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "milestone_item", indexes = {
        @Index(name = "idx_milestone_item_report", columnList = "report_id"),
        @Index(name = "idx_milestone_item_driving", columnList = "driving_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK: milestone_report.id */
    @Column(name = "report_id", nullable = false)
    private Long reportId;

    /** 주행 ID */
    @Column(name = "driving_id", nullable = false, length = 50)
    private String drivingId;

    /** 순서 번호 (1, 2, 3, ...) */
    @Column(name = "order_no", nullable = false)
    private Integer orderNo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public static MilestoneItem of(MilestoneReport report, String drivingId, int orderNo) {
        return MilestoneItem.builder()
                .reportId(report.getId())
                .drivingId(drivingId)
                .orderNo(orderNo)
                .build();
    }
}