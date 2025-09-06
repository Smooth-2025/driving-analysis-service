package com.smooth.driving_analysis_service.reports.basic_summary.repository;

import com.smooth.driving_analysis_service.reports.basic_summary.entity.DrivingAccumulatedStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DrivingAccumulatedStatsRepository extends JpaRepository<DrivingAccumulatedStats, Long> {
    
    List<DrivingAccumulatedStats> findByDrivingIdIn(List<String> drivingIds);
    
    List<DrivingAccumulatedStats> findByUserId(Long userId);
    
    @Query(value = """
        SELECT 
            SUM(das.total_distance) / 1000.0 AS totalDistanceKm,
            AVG(das.driving_minutes * 60) AS averageDurationSec,
            AVG(das.total_distance) / 1000.0 AS averageDistanceKm,
            AVG(das.avg_speed) AS averageSpeedKmh,
            AVG(das.cruise_ratio) AS averageCruiseRatio,
            MIN(DATE(das.start_time)) AS periodStart,
            MAX(DATE(das.end_time)) AS periodEnd
        FROM driving_accumulated_stats das
        JOIN milestone_item mi ON mi.driving_id = das.driving_id
        WHERE mi.report_id = :reportId
        """, nativeQuery = true)
    BasicSummaryProjection getBasicSummaryByReportId(@Param("reportId") Long reportId);
    
    interface BasicSummaryProjection {
        Double getTotalDistanceKm();
        Double getAverageDurationSec();
        Double getAverageDistanceKm();
        Double getAverageSpeedKmh();
        Double getAverageCruiseRatio();
        LocalDate getPeriodStart();
        LocalDate getPeriodEnd();
    }
}