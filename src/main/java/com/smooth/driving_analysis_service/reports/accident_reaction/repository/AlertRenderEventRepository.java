package com.smooth.driving_analysis_service.reports.accident_reaction.repository;

import com.smooth.driving_analysis_service.reports.accident_reaction.entity.AlertRenderEvent;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRenderEventRepository extends JpaRepository<AlertRenderEvent, Long> {
    
    List<AlertRenderEvent> findByUserId(Long userId);
    
    List<AlertRenderEvent> findByDrivingId(String drivingId);
    
    boolean existsByAlertId(String alertId);
    
    /**
     * 특정 리포트에 속한 AlertRenderEvent 조회
     */
    @Query("""
        SELECT are FROM AlertRenderEvent are
        JOIN MilestoneItem mi ON mi.drivingId = are.drivingId
        WHERE mi.reportId = :reportId
        """)
    List<AlertRenderEvent> findByReportId(@Param("reportId") Long reportId);
}