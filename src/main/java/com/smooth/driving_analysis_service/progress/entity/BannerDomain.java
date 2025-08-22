package com.smooth.driving_analysis_service.progress.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "banner")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class BannerDomain {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @Column(name = "acked_reports", nullable = false)
    private Integer ackedReports;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
