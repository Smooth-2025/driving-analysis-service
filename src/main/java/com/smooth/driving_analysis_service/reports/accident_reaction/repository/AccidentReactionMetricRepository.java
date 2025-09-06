package com.smooth.driving_analysis_service.reports.accident_reaction.repository;

import com.smooth.driving_analysis_service.reports.accident_reaction.entity.AccidentReactionMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AccidentReactionMetricRepository extends JpaRepository<AccidentReactionMetric, String> {
    
    List<AccidentReactionMetric> findByDrivingIdIn(List<String> drivingIds);
    
    List<AccidentReactionMetric> findByUserId(Long userId);
    
    List<AccidentReactionMetric> findByDrivingId(String drivingId);
    
    boolean existsByAlertId(String alertId);
    
    void deleteByAlertId(String alertId);
    
    /**
     * Task 1: 리포트 ID로 기본 반응 지표 조회
     * AlertRenderEvent와 AccidentReactionMetric을 조인하여 해당 리포트의 알림 데이터 집계
     * milestone_item을 통해 해당 리포트에 속한 drivingId만 필터링
     */
    @Query("""
        SELECT 
            COUNT(are.alertId) as receivedAlertCount,
            AVG(CASE WHEN arm.responded = true THEN arm.reactionMs / 1000.0 END) as avgReactionSec,
            AVG(CASE WHEN arm.decelOrStop = true THEN 1.0 ELSE 0.0 END) as brakeOrStopRatio,
            AVG(CASE WHEN arm.evasiveManeuver = true THEN 1.0 ELSE 0.0 END) as avoidRatio
        FROM AlertRenderEvent are
        LEFT JOIN AccidentReactionMetric arm ON are.alertId = arm.alertId
        JOIN MilestoneItem mi ON mi.drivingId = are.drivingId
        WHERE mi.reportId = :reportId
        """)
    Object[] getBasicMetricsByReportId(@Param("reportId") Long reportId);
    
    /**
     * Task 2: 전체 사용자 평균 반응시간 조회 (벤치마크용)
     */
    @Query("""
        SELECT AVG(arm.reactionMs / 1000.0) 
        FROM AccidentReactionMetric arm 
        WHERE arm.responded = true
        """)
    Double getGlobalAverageReactionTime();
}