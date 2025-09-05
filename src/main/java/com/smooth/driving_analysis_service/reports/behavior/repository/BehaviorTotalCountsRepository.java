package com.smooth.driving_analysis_service.reports.behavior.repository;

import com.smooth.driving_analysis_service.reports.behavior.dto.response.TotalCountsDto;
import com.smooth.driving_analysis_service.pipeline.entity.DrivingAccumulatedStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BehaviorTotalCountsRepository extends JpaRepository<DrivingAccumulatedStats, Long> {
    
    @Query("""
        SELECT new com.smooth.driving_analysis_service.reports.behavior.dto.response.TotalCountsDto(
            COALESCE(SUM(das.hardBrakeCount), 0),
            COALESCE(SUM(das.rapidAccelCount), 0), 
            COALESCE(SUM(das.laneChangeCount), 0)
        )
        FROM DrivingAccumulatedStats das
        JOIN com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneItem mi ON mi.drivingId = das.drivingId
        WHERE mi.reportId = :reportId
        """)
    TotalCountsDto findTotalCountsByReportId(@Param("reportId") Long reportId);
}