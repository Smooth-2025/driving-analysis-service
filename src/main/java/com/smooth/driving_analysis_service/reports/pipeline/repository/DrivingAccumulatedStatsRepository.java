package com.smooth.driving_analysis_service.reports.pipeline.repository;

import com.smooth.driving_analysis_service.reports.pipeline.entity.DrivingAccumulatedStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DrivingAccumulatedStatsRepository extends JpaRepository<DrivingAccumulatedStats, Long> {
    
    Optional<DrivingAccumulatedStats> findByDrivingId(String drivingId);
    
    Optional<DrivingAccumulatedStats> findByUserIdAndDrivingId(Long userId, String drivingId);
    
    boolean existsByDrivingId(String drivingId);
    
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
    
    // 타임라인용 메서드들 추가
    @Query("SELECT das FROM DrivingAccumulatedStats das WHERE das.userId = :userId ORDER BY das.endTime DESC")
    org.springframework.data.domain.Page<DrivingAccumulatedStats> findByUserIdOrderByEndTimeDesc(
            @Param("userId") Long userId, 
            org.springframework.data.domain.Pageable pageable);
    
    @Query("SELECT das FROM DrivingAccumulatedStats das WHERE das.userId = :userId AND das.endTime < :before ORDER BY das.endTime DESC")
    org.springframework.data.domain.Page<DrivingAccumulatedStats> findByUserIdAndEndTimeBeforeOrderByEndTimeDesc(
            @Param("userId") Long userId, 
            @Param("before") java.time.LocalDateTime before,
            org.springframework.data.domain.Pageable pageable);
    
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