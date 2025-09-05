// reports/accident_reaction/entity/V2xResponseScore.java
package com.smooth.driving_analysis_service.reports.accident_reaction.entity;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;

@Entity @Table(name="v2x_response_score",
        uniqueConstraints=@UniqueConstraint(name="uk_v2x", columnNames={"report_id","snapshot_type"}))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class V2xResponseScore {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private Long reportId;
    @Enumerated(EnumType.STRING) @Column(length=10) private SnapshotType snapshotType; // INTERIM|FINAL
    private int totalAlerts; private int reactedAlerts;
    private double reactionRate; private double avgReactionMs;
    private double decelRate; private double evasiveRate;
    private LocalDateTime createdAt; private LocalDateTime updatedAt;
    public enum SnapshotType { INTERIM, FINAL }
}
