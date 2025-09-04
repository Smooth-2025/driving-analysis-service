package com.smooth.driving_analysis_service.reports.accident_reaction.repository;

import com.smooth.driving_analysis_service.reports.accident_reaction.entity.AccidentReactionMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AccidentReactionMetricRepository extends JpaRepository<AccidentReactionMetric, String> {
    List<AccidentReactionMetric> findByDrivingIdIn(List<String> drivingIds);
    
    // 사용자별 평균 반응시간 (반응한 경우만)
    @Query("SELECT AVG(a.reactionMs) FROM AccidentReactionMetric a WHERE a.userId = :userId AND a.reacted = true AND a.reactionMs IS NOT NULL")
    Double findAvgReactionMsByUserId(@Param("userId") Long userId);
    
    // 전체 사용자 평균 반응시간 (반응한 경우만)
    @Query("SELECT AVG(a.reactionMs) FROM AccidentReactionMetric a WHERE a.reacted = true AND a.reactionMs IS NOT NULL")
    Double findGlobalAvgReactionMs();
    
    // 특정 반응시간보다 느린 사용자 비율 계산용
    @Query("SELECT COUNT(DISTINCT a.userId) FROM AccidentReactionMetric a WHERE a.reacted = true AND a.reactionMs IS NOT NULL")
    Long countDistinctUsersWithReaction();
    
    @Query("SELECT COUNT(DISTINCT a.userId) FROM AccidentReactionMetric a WHERE a.reacted = true AND a.reactionMs > :reactionMs")
    Long countDistinctUsersSlowerThan(@Param("reactionMs") Integer reactionMs);
    
    // 사용자별 알림 수
    Long countByUserId(Long userId);
    
    // 사용자 반응 통계
    @Query(value = """
        SELECT 
            COUNT(CASE WHEN decel_or_stop = true THEN 1 END) / COUNT(*) as brakeRatio,
            COUNT(CASE WHEN evasive_maneuver = true THEN 1 END) / COUNT(*) as avoidRatio
        FROM accident_reaction_metric 
        WHERE user_id = :userId AND reacted = true
        """, nativeQuery = true)
    Map<String, Object> getUserReactionStats(@Param("userId") Long userId);
    
    // Alert Render Event 저장 (멱등)
    @Modifying
    @Query(value = """
        INSERT INTO alert_render_event (alert_id, user_id, driving_id, rendered_at, type, created_at)
        VALUES (:alertId, :userId, :drivingId, :renderedAt, :type, NOW())
        ON DUPLICATE KEY UPDATE
        driving_id = VALUES(driving_id),
        rendered_at = VALUES(rendered_at),
        type = VALUES(type)
        """, nativeQuery = true)
    void upsertAlertRender(@Param("alertId") String alertId, 
                          @Param("userId") Long userId,
                          @Param("drivingId") String drivingId, 
                          @Param("renderedAt") LocalDateTime renderedAt,
                          @Param("type") String type);
    
    // Reaction Metric 저장 (멱등)
    @Modifying
    @Query(value = """
        INSERT INTO accident_reaction_metric (alert_id, user_id, driving_id, rendered_at, reacted, reaction_ms, event_type, decel_or_stop, evasive_maneuver, created_at, updated_at)
        VALUES (:alertId, :userId, :drivingId, :renderedAt, :reacted, :reactionMs, :eventType, :decelOrStop, :evasiveManeuver, NOW(), NOW())
        ON DUPLICATE KEY UPDATE
        reacted = VALUES(reacted),
        reaction_ms = VALUES(reaction_ms),
        event_type = VALUES(event_type),
        decel_or_stop = VALUES(decel_or_stop),
        evasive_maneuver = VALUES(evasive_maneuver),
        updated_at = NOW()
        """, nativeQuery = true)
    void upsertReactionMetric(@Param("alertId") String alertId,
                             @Param("userId") Long userId,
                             @Param("drivingId") String drivingId,
                             @Param("renderedAt") LocalDateTime renderedAt,
                             @Param("reacted") boolean reacted,
                             @Param("reactionMs") Integer reactionMs,
                             @Param("eventType") String eventType,
                             @Param("decelOrStop") boolean decelOrStop,
                             @Param("evasiveManeuver") boolean evasiveManeuver);
    
    // 요약 통계
    @Query(value = """
        SELECT 
            COUNT(*) as totalAlerts,
            AVG(CASE WHEN reacted = true THEN reaction_ms END) as avgReactionMs,
            COUNT(CASE WHEN reacted = true THEN 1 END) as respondedCount,
            COUNT(CASE WHEN decel_or_stop = true THEN 1 END) as decelCount,
            COUNT(CASE WHEN evasive_maneuver = true THEN 1 END) as evasiveCount
        FROM accident_reaction_metric 
        WHERE user_id = :userId 
        AND rendered_at BETWEEN :fromDate AND :toDate
        """, nativeQuery = true)
    Map<String, Object> getSummaryStats(@Param("userId") Long userId,
                                       @Param("fromDate") LocalDateTime fromDate,
                                       @Param("toDate") LocalDateTime toDate);
}
