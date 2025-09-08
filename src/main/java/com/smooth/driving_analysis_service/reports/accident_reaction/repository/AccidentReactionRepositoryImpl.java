package com.smooth.driving_analysis_service.reports.accident_reaction.repository;

import com.smooth.driving_analysis_service.reports.accident_reaction.entity.AccidentReactionMetric;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Repository
@RequiredArgsConstructor
public class AccidentReactionRepositoryImpl implements AccidentReactionCustomRepository {
    
    @PersistenceContext 
    private final EntityManager em;

    // Custom methods implementation
    @Transactional
    public void upsertAlertRender(String alertId, Long userId, String drivingId, LocalDateTime renderedAt, String type) {
        em.createNativeQuery("""
            INSERT INTO alert_render_event
              (alert_id, user_id, driving_id, rendered_at, type, created_at)
            VALUES (:aid, :uid, :did, :rt, :type, NOW())
            ON DUPLICATE KEY UPDATE
              user_id=:uid, driving_id=:did, rendered_at=:rt, type=:type
        """)
                .setParameter("aid", alertId)
                .setParameter("uid", userId)
                .setParameter("did", drivingId)
                .setParameter("rt", Timestamp.valueOf(renderedAt))
                .setParameter("type", type)
                .executeUpdate();
    }

    @Transactional
    public void upsertReactionMetric(String alertId, Long userId, String drivingId, LocalDateTime renderedAt,
                                     boolean responded, Integer reactionMs, String eventType,
                                     boolean decelOrStop, boolean evasiveManeuver) {
        em.createNativeQuery("""
            INSERT INTO accident_reaction_metric
              (alert_id, user_id, driving_id, rendered_at, reacted, reaction_ms, event_type,
               decel_or_stop, evasive_maneuver, created_at, updated_at)
            VALUES (:aid,:uid,:did,:rt,:resp,:rms,:et,:decel,:evas,NOW(),NOW())
            ON DUPLICATE KEY UPDATE
              user_id=:uid, driving_id=:did, rendered_at=:rt,
              reacted=:resp, reaction_ms=:rms, event_type=:et,
              decel_or_stop=:decel, evasive_maneuver=:evas, updated_at=NOW()
        """)
                .setParameter("aid", alertId)
                .setParameter("uid", userId)
                .setParameter("did", drivingId)
                .setParameter("rt", Timestamp.valueOf(renderedAt))
                .setParameter("resp", responded ? 1 : 0)
                .setParameter("rms", reactionMs)
                .setParameter("et", eventType)
                .setParameter("decel", decelOrStop ? 1 : 0)
                .setParameter("evas", evasiveManeuver ? 1 : 0)
                .executeUpdate();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSummaryStats(Long userId, LocalDateTime from, LocalDateTime to) {
        var r = (Object[]) em.createNativeQuery("""
            SELECT
              COUNT(*)                                                    AS total_alerts,
              COALESCE(SUM(responded),0)                                  AS reacted_alerts,
              CASE WHEN COUNT(*)=0 THEN 0 ELSE COALESCE(SUM(responded),0)/COUNT(*) END AS reaction_rate,
              COALESCE(AVG(CASE WHEN responded=1 THEN response_time_ms END),0)   AS avg_reaction_ms,
              CASE WHEN COALESCE(SUM(responded),0)=0 THEN 0
                   ELSE COALESCE(SUM(decel_or_stop),0)/COALESCE(SUM(responded),0) END  AS decel_rate,
              CASE WHEN COALESCE(SUM(responded),0)=0 THEN 0
                   ELSE COALESCE(SUM(evasive_maneuver),0)/COALESCE(SUM(responded),0) END AS evasive_rate
            FROM accident_reaction_metric
            WHERE user_id=:uid AND rendered_at BETWEEN :from AND :to
        """).setParameter("uid", userId)
                .setParameter("from", Timestamp.valueOf(from))
                .setParameter("to", Timestamp.valueOf(to))
                .getSingleResult();

        return Map.of(
                "totalAlerts",   ((Number) r[0]).intValue(),
                "reactedAlerts", ((Number) r[1]).intValue(),
                "reactionRate",  r[2]==null?0:((Number) r[2]).doubleValue(),
                "avgReactionMs", r[3]==null?0:((Number) r[3]).doubleValue(),
                "decelRate",     r[4]==null?0:((Number) r[4]).doubleValue(),
                "evasiveRate",   r[5]==null?0:((Number) r[5]).doubleValue()
        );
    }

    @Transactional(readOnly = true)
    public Double findAvgReactionMsByUserId(Long userId) {
        return (Double) em.createNativeQuery("""
            SELECT AVG(CASE WHEN reacted = 1 THEN reaction_ms END)
            FROM accident_reaction_metric
            WHERE user_id = :userId AND reacted = 1
        """).setParameter("userId", userId).getSingleResult();
    }

    @Transactional(readOnly = true)
    public Double findGlobalAvgReactionMs() {
        return (Double) em.createNativeQuery("""
            SELECT AVG(CASE WHEN reacted = 1 THEN reaction_ms END)
            FROM accident_reaction_metric
            WHERE reacted = 1
        """).getSingleResult();
    }

    @Transactional(readOnly = true)
    public Long countByUserId(Long userId) {
        return ((Number) em.createNativeQuery("""
            SELECT COUNT(*)
            FROM accident_reaction_metric
            WHERE user_id = :userId
        """).setParameter("userId", userId).getSingleResult()).longValue();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUserReactionStats(Long userId) {
        var r = (Object[]) em.createNativeQuery("""
            SELECT
              COUNT(*) as total_alerts,
              COALESCE(SUM(reacted),0) as reacted_alerts,
              COALESCE(AVG(CASE WHEN reacted=1 THEN reaction_ms END),0) as avg_reaction_ms
            FROM accident_reaction_metric
            WHERE user_id = :userId
        """).setParameter("userId", userId).getSingleResult();

        return Map.of(
                "totalAlerts", ((Number) r[0]).longValue(),
                "reactedAlerts", ((Number) r[1]).longValue(),
                "avgReactionMs", r[2] == null ? 0.0 : ((Number) r[2]).doubleValue()
        );
    }

    @Transactional(readOnly = true)
    public Long countDistinctUsersWithReaction() {
        return ((Number) em.createNativeQuery("""
            SELECT COUNT(DISTINCT user_id)
            FROM accident_reaction_metric
            WHERE reacted = 1
        """).getSingleResult()).longValue();
    }

    @Transactional(readOnly = true)
    public Long countDistinctUsersSlowerThan(int reactionTimeMs) {
        return ((Number) em.createNativeQuery("""
            SELECT COUNT(DISTINCT user_id)
            FROM accident_reaction_metric
            WHERE reacted = 1 AND reaction_ms > :reactionTimeMs
        """).setParameter("reactionTimeMs", reactionTimeMs).getSingleResult()).longValue();
    }

    @Override
    @Transactional(readOnly = true)
    public Double getGlobalAverageReactionTime() {
        return (Double) em.createNativeQuery("""
            SELECT AVG(CASE WHEN reacted = 1 THEN reaction_ms / 1000.0 END)
            FROM accident_reaction_metric
            WHERE reacted = 1
        """).getSingleResult();
    }
}