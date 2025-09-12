// entity/DnaSnapshot.java
package com.smooth.driving_analysis_service.reports.dna.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name="dna_snapshot",
        indexes = { @Index(name="ix_dna_snapshot_user", columnList="user_id") },
        uniqueConstraints = { @UniqueConstraint(name="uk_dna_snapshot_report", columnNames={"report_id"}) }
)
public class DnaSnapshot {

    public enum Status { INTERIM, FINAL }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="report_id", nullable=false, length=100)
    private String reportId; // u{userId}_c{cycleNo}_interim 또는 u{userId}_c{cycleNo}_final_{yyyyMMdd}

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable=false, length=16)
    private Status status;

    @Column(name="code", nullable=false, length=16)
    private String code;     // e.g., "A2-B1-C3-D2"

    @Column(name="score_a", nullable=false)
    private int scoreA;
    @Column(name="score_b", nullable=false)
    private int scoreB;
    @Column(name="score_c", nullable=false)
    private int scoreC;
    @Column(name="score_d", nullable=false)
    private int scoreD;
    
    // 새로운 사이클 기반 시스템용 점수 필드들
    @Column(name="safe_driving_score")
    private Double safeDrivingScore;
    @Column(name="eco_driving_score")
    private Double ecoDrivingScore;
    @Column(name="defensive_driving_score")
    private Double defensiveDrivingScore;
    @Column(name="smooth_driving_score")
    private Double smoothDrivingScore;

    @Column(name="headline", length=255)
    private String headline;

    // 야간 배치 메타데이터
    @Column(name="last_interim_count")
    private Integer lastInterimCount;

    @Column(name="last_interim_at")
    private LocalDateTime lastInterimAt;

    @CreationTimestamp
    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;
}
