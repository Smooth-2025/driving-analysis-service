// reports/accident_reaction/snapshot/AccidentReactionSnapshotWriterImpl.java
package com.smooth.driving_analysis_service.reports.accident_reaction.snapshot;

import jakarta.persistence.*; import lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository; import org.springframework.transaction.annotation.Transactional;

@Slf4j @Repository @RequiredArgsConstructor
public class AccidentReactionSnapshotWriterImpl implements AccidentReactionSnapshotWriter {
    @PersistenceContext private final EntityManager em;

    @Override @Transactional
    public void upsertV2xResponse(Long reportId, String snapshotType) {
        em.createNativeQuery("""
            INSERT INTO v2x_response_score
              (report_id, snapshot_type, total_alerts, reacted_alerts, reaction_rate,
               avg_reaction_ms, decel_rate, evasive_rate, created_at, updated_at)
            SELECT
              :rid, :stype,
              COUNT(*),
              COALESCE(SUM(arm.reacted), 0),
              CASE WHEN COUNT(*)=0 THEN 0 ELSE COALESCE(SUM(arm.reacted),0)/COUNT(*) END,
              COALESCE(AVG(CASE WHEN arm.reacted=1 THEN arm.reaction_ms END), 0),
              CASE WHEN COALESCE(SUM(arm.reacted),0)=0 THEN 0
                   ELSE COALESCE(SUM(arm.decel_or_stop),0)/COALESCE(SUM(arm.reacted),0) END,
              CASE WHEN COALESCE(SUM(arm.reacted),0)=0 THEN 0
                   ELSE COALESCE(SUM(arm.evasive_maneuver),0)/COALESCE(SUM(arm.reacted),0) END,
              NOW(), NOW()
            FROM milestone_item mi
            JOIN accident_reaction_metric arm ON arm.driving_id = mi.driving_id
            WHERE mi.report_id = :rid
            ON DUPLICATE KEY UPDATE
              total_alerts    = VALUES(total_alerts),
              reacted_alerts  = VALUES(reacted_alerts),
              reaction_rate   = VALUES(reaction_rate),
              avg_reaction_ms = VALUES(avg_reaction_ms),
              decel_rate      = VALUES(decel_rate),
              evasive_rate    = VALUES(evasive_rate),
              updated_at      = NOW()
        """).setParameter("rid", reportId)
                .setParameter("stype", snapshotType)
                .executeUpdate();
        log.info("[V2X] snapshot upsert reportId={} type={}", reportId, snapshotType);
    }
}
