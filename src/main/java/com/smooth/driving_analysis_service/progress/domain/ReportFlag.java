package com.smooth.driving_analysis_service.progress.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_flag")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReportFlag {

    @Id
    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;

    @Column(name = "eligible", nullable = false)
    private boolean eligible;             // 15회 달성 시 true (멱등)

    @Column(name = "eligible_at")
    private LocalDateTime eligibleAt;     // true로 처음 세팅된 시각

    @Column(name = "banner_ack", nullable = false)
    private boolean bannerAck;            // 배너 확인(닫힘) 여부

    @Column(name = "banner_ack_at")
    private LocalDateTime bannerAckAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
