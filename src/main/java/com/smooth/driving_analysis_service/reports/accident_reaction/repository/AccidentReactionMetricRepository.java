package com.smooth.driving_analysis_service.reports.accident_reaction.repository;

import com.smooth.driving_analysis_service.reports.accident_reaction.entity.AccidentReactionMetric;
import com.smooth.driving_analysis_service.reports.accident_reaction.entity.AlertRenderEvent;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccidentReactionMetricRepository extends JpaRepository<AccidentReactionMetric, Long> {
    
    List<AccidentReactionMetric> findByDrivingId(String drivingId);
    
    List<AccidentReactionMetric> findByUserId(Long userId);
    
    List<AccidentReactionMetric> findByAlertId(String alertId);
    
    /**
     * 특정 리포트의 사고 반응 기본 지표 조회
     */
    @Query("""
        SELECT 
            COUNT(are.alertId) as receivedAlertCount,
            AVG(CASE WHEN arm.reacted = true THEN arm.reactionMs / 1000.0 END) as avgReactionSec,
            AVG(CASE WHEN arm.decelOrStop = true THEN 1.0 ELSE 0.0 END) as brakeOrStopRatio,
            AVG(CASE WHEN arm.evasiveManeuver = true THEN 1.0 ELSE 0.0 END) as avoidRatio
        FROM AlertRenderEvent are
        LEFT JOIN AccidentReactionMetric arm ON are.alertId = arm.alertId
        JOIN MilestoneItem mi ON mi.drivingId = are.drivingId
        WHERE mi.reportId = :reportId
        """)
    Object[] getBasicMetricsByReportId(@Param("reportId") Long reportId);
    
    /**
     * 전체 사용자 평균 반응시간 조회 (벤치마크용)
     */
    @Query("""
        SELECT AVG(arm.reactionMs / 1000.0) 
        FROM AccidentReactionMetric arm 
        WHERE arm.reacted = true AND arm.reactionMs IS NOT NULL
        """)
    Double getGlobalAverageReactionTime();
}