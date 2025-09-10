package com.smooth.driving_analysis_service.reports.accident_reaction.repository;

import com.smooth.driving_analysis_service.reports.accident_reaction.entity.AccidentReactionMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
     * drivingId가 null인 경우도 고려하여 LEFT JOIN 사용
     */
    @Query("""
        SELECT 
            COUNT(are.alertId) as receivedAlertCount,
            AVG(CASE WHEN arm.reacted = true THEN arm.reactionMs / 1000.0 END) as avgReactionSec,
            AVG(CASE WHEN arm.decelOrStop = true THEN 1.0 ELSE 0.0 END) as brakeOrStopRatio,
            AVG(CASE WHEN arm.evasiveManeuver = true THEN 1.0 ELSE 0.0 END) as avoidRatio
        FROM AlertRenderEvent are
        LEFT JOIN AccidentReactionMetric arm ON are.alertId = arm.alertId
        LEFT JOIN MilestoneItem mi ON mi.drivingId = are.drivingId
        WHERE mi.reportId = :reportId AND are.drivingId IS NOT NULL
        """)
    Object[] getBasicMetricsByReportId(@Param("reportId") Long reportId);
    
    /**
     * Task 2: 전체 사용자 평균 반응시간 조회 (벤치마크용)
     */
    @Query("""
        SELECT AVG(arm.reactionMs / 1000.0) 
        FROM AccidentReactionMetric arm 
        WHERE arm.reacted = true AND arm.reactionMs IS NOT NULL
        """)
    Double getGlobalAverageReactionTime();
    
    /**
     * 디버깅용: 특정 리포트의 AlertRenderEvent 중 drivingId가 null인 개수 조회
     */
    @Query("""
        SELECT COUNT(are.alertId)
        FROM AlertRenderEvent are
        LEFT JOIN MilestoneItem mi ON mi.drivingId = are.drivingId
        WHERE mi.reportId = :reportId AND are.drivingId IS NULL
        """)
    Long countNullDrivingIdsByReportId(@Param("reportId") Long reportId);
    
    /**
     * 사용자별 리포트 ID로 기본 반응 지표 조회
     */
    @Query("""
        SELECT 
            COUNT(arm.alertId) as receivedAlertCount,
            AVG(CASE WHEN arm.reacted = true THEN arm.reactionMs / 1000.0 END) as avgReactionSec,
            AVG(CASE WHEN arm.decelOrStop = true THEN 1.0 ELSE 0.0 END) as brakeOrStopRatio,
            AVG(CASE WHEN arm.evasiveManeuver = true THEN 1.0 ELSE 0.0 END) as avoidRatio
        FROM AccidentReactionMetric arm
        JOIN MilestoneItem mi ON mi.drivingId = arm.drivingId
        WHERE arm.userId = :userId AND mi.reportId = :reportId
        """)
    Object[] getBasicMetricsByUserIdAndReportId(@Param("userId") Long userId, @Param("reportId") Long reportId);
    
    /**
     * 대안 쿼리: userId 기반으로 해당 리포트의 주행 기록들과 연관된 알림 데이터 조회
     * MilestoneItem을 통해 해당 리포트의 drivingId 목록을 가져와서 매칭
     */
    @Query("""
        SELECT 
            COUNT(are.alertId) as receivedAlertCount,
            AVG(CASE WHEN arm.reacted = true THEN arm.reactionMs / 1000.0 END) as avgReactionSec,
            AVG(CASE WHEN arm.decelOrStop = true THEN 1.0 ELSE 0.0 END) as brakeOrStopRatio,
            AVG(CASE WHEN arm.evasiveManeuver = true THEN 1.0 ELSE 0.0 END) as avoidRatio
        FROM AlertRenderEvent are
        LEFT JOIN AccidentReactionMetric arm ON are.alertId = arm.alertId
        WHERE are.drivingId IN (
            SELECT mi.drivingId FROM MilestoneItem mi WHERE mi.reportId = :reportId
        )
        """)
    Object[] getBasicMetricsByReportIdAlternative(@Param("reportId") Long reportId);
}