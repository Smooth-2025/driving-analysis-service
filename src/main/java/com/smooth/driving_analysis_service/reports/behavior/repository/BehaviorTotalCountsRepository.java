package com.smooth.driving_analysis_service.reports.behavior.repository;

import com.smooth.driving_analysis_service.reports.behavior.dto.response.BehaviorAnalysisResponseDto;
import com.smooth.driving_analysis_service.reports.pipeline.entity.DrivingAccumulatedStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BehaviorTotalCountsRepository extends JpaRepository<DrivingAccumulatedStats, Long> {
    
    @Query("""
        SELECT new com.smooth.driving_analysis_service.reports.behavior.dto.response.BehaviorAnalysisResponseDto$TotalCounts(
            CAST(COALESCE(SUM(das.hardBrakeCount), 0) AS int),
            CAST(COALESCE(SUM(das.rapidAccelCount), 0) AS int), 
            CAST(COALESCE(SUM(das.laneChangeCount), 0) AS int)
        )
        FROM DrivingAccumulatedStats das
        JOIN com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneItem mi ON mi.drivingId = das.drivingId
        WHERE mi.reportId = :reportId
        """)
    BehaviorAnalysisResponseDto.TotalCounts findTotalCountsByReportId(@Param("reportId") Long reportId);
    
    @Query("""
        SELECT new com.smooth.driving_analysis_service.reports.behavior.dto.response.BehaviorAnalysisResponseDto$TotalCounts(
            CAST(COALESCE(SUM(das.hardBrakeCount), 0) AS int),
            CAST(COALESCE(SUM(das.rapidAccelCount), 0) AS int), 
            CAST(COALESCE(SUM(das.laneChangeCount), 0) AS int)
        )
        FROM DrivingAccumulatedStats das
        WHERE das.drivingId IN :drivingIds
        """)
    BehaviorAnalysisResponseDto.TotalCounts findTotalCountsByDrivingIds(@Param("drivingIds") java.util.List<String> drivingIds);
    
    @Query(value = """
        SELECT 
            COALESCE(SUM(das.hard_brake_count), 0) as hardBrake,
            COALESCE(SUM(das.rapid_accel_count), 0) as rapidAccel,
            COALESCE(SUM(das.lane_change_count), 0) as laneChange
        FROM driving_accumulated_stats das
        JOIN milestone_item mi ON mi.driving_id = das.driving_id
        WHERE mi.report_id = :reportId
        """, nativeQuery = true)
    TotalCountsProjection getTotalCountsByReportId(@Param("reportId") Long reportId);
    
    interface TotalCountsProjection {
        Integer getHardBrake();
        Integer getRapidAccel();
        Integer getLaneChange();
    }
}