package com.smooth.driving_analysis_service.reports.milestone.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "milestone_item",
        indexes = {
                @Index(name="ix_milestone_item_report", columnList="report_id"),
                @Index(name="ix_milestone_item_report_order", columnList="report_id,order_no", unique = true)
        })
public class MilestoneItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="report_id", nullable=false)
    private Long reportId;

    @Column(name="driving_id", nullable=false, length=64)
    private String drivingId;

    @Column(name="order_no", nullable=false)
    private Integer orderNo;

    @CreationTimestamp
    @Column(name="created_at", updatable=false)
    private LocalDateTime createdAt;
}
