package com.smooth.driving_analysis_service.reports.basic_summary.repository;

import com.smooth.driving_analysis_service.reports.basic_summary.entity.BasicSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BasicSummaryRepository extends JpaRepository<BasicSummary, Long> {

    /**
     * 리포트별 특정 타입의 요약 조회
     */
    Optional<BasicSummary> findByReportIdAndSnapshotType(Long reportId, BasicSummary.SnapshotType snapshotType);

    /**
     * 사용자별 최신 FINAL 요약 조회
     */
    Optional<BasicSummary> findTopByUserIdAndSnapshotTypeOrderByCreatedAtDesc(
            Long userId, BasicSummary.SnapshotType snapshotType);

    /**
     * driving_accumulated_stats 기반 요약 데이터 조회
     */
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
    BasicSummaryProjection calculateSummaryByReportId(@Param("reportId") Long reportId);

    /**
     * 요약 데이터 프로젝션 인터페이스
     */
    interface BasicSummaryProjection {
        Double getTotalDistanceKm();
        Double getAverageDurationSec();
        Double getAverageDistanceKm();
        Double getAverageSpeedKmh();
        Double getAverageCruiseRatio();
        String getPeriodStart();
        String getPeriodEnd();
    }
}