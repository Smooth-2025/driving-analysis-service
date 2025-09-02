package com.smooth.driving_analysis_service.reports.behavior.repository;

import com.smooth.driving_analysis_service.driving.entity.DrivingRecord;
import com.smooth.driving_analysis_service.reports.behavior.dto.result.BehaviorSummaryResultDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BehaviorDrivingRecordRepository extends JpaRepository<DrivingRecord, String> {

    /**
     * 현재 report 기준 hardBrake, rapidAccel, laneChange 합계 조회
     */
    @Query("""
        SELECT new com.smooth.driving_analysis_service.reports.behavior.dto.BehaviorSummaryResultDto(
            COALESCE(SUM(dr.hardBrakeCount),0),
            COALESCE(SUM(dr.rapidAccelCount),0),
            COALESCE(SUM(dr.laneChangeCount),0)
        )
        FROM DrivingRecord dr
        JOIN dr.milestoneItem mi
        WHERE mi.reportId = :reportId
        """)
    BehaviorSummaryResultDto fetchSummary(@Param("reportId") Long reportId);

    /**
     * 이전 report 기준 hardBrake, rapidAccel, laneChange 합계 조회
     */
    @Query("""
        SELECT new com.smooth.driving_analysis_service.reports.behavior.dto.BehaviorSummaryResultDto(
            COALESCE(SUM(dr.hardBrakeCount),0),
            COALESCE(SUM(dr.rapidAccelCount),0),
            COALESCE(SUM(dr.laneChangeCount),0)
        )
        FROM DrivingRecord dr
        JOIN dr.milestoneItem mi
        WHERE mi.reportId = :prevReportId
        """)
    BehaviorSummaryResultDto fetchPrevSummary(@Param("prevReportId") Long prevReportId);

    /**
     * 요일×시간대×행동별 dominant point 조회
     * (NativeQuery 그대로 두고 Object[] 반환 가능)
     */
    @Query(value = """
        WITH base AS (
          SELECT
            ((DAYOFWEEK(dr.end_time) + 5) % 7) + 1 AS dow,
            CASE
              WHEN HOUR(dr.end_time) < 6  THEN 'DAWN'
              WHEN HOUR(dr.end_time) < 10 THEN 'COMMUTE'
              WHEN HOUR(dr.end_time) < 17 THEN 'DAY'
              WHEN HOUR(dr.end_time) < 20 THEN 'OFFWORK'
              ELSE 'EVENING'
            END AS timeSlot,
            'HARD_BRAKE' AS behavior, dr.hard_brake_count AS cnt
          FROM driving_analysis.driving_record dr
          JOIN driving_analysis.milestone_item mi ON mi.driving_id = dr.driving_id
          WHERE mi.report_id = :reportId
          UNION ALL
          SELECT
            ((DAYOFWEEK(dr.end_time) + 5) % 7) + 1,
            CASE WHEN HOUR(dr.end_time) < 6 THEN 'DAWN'
                 WHEN HOUR(dr.end_time) < 10 THEN 'COMMUTE'
                 WHEN HOUR(dr.end_time) < 17 THEN 'DAY'
                 WHEN HOUR(dr.end_time) < 20 THEN 'OFFWORK'
                 ELSE 'EVENING' END,
            'RAPID_ACCEL', dr.rapid_accel_count
          FROM driving_analysis.driving_record dr
          JOIN driving_analysis.milestone_item mi ON mi.driving_id = dr.driving_id
          WHERE mi.report_id = :reportId
          UNION ALL
          SELECT
            ((DAYOFWEEK(dr.end_time) + 5) % 7) + 1,
            CASE WHEN HOUR(dr.end_time) < 6 THEN 'DAWN'
                 WHEN HOUR(dr.end_time) < 10 THEN 'COMMUTE'
                 WHEN HOUR(dr.end_time) < 17 THEN 'DAY'
                 WHEN HOUR(dr.end_time) < 20 THEN 'OFFWORK'
                 ELSE 'EVENING' END,
            'LANE_CHANGE', dr.lane_change_count
          FROM driving_analysis.driving_record dr
          JOIN driving_analysis.milestone_item mi ON mi.driving_id = dr.driving_id
          WHERE mi.report_id = :reportId
        ),
        agg AS (
          SELECT dow, timeSlot, behavior, SUM(cnt) AS cnt
          FROM base
          GROUP BY dow, timeSlot, behavior
        ),
        ranked AS (
          SELECT a.*,
                 ROW_NUMBER() OVER (
                   PARTITION BY dow, behavior
                   ORDER BY cnt DESC,
                     CASE timeSlot
                       WHEN 'DAWN'    THEN 1
                       WHEN 'DAY'     THEN 2
                       WHEN 'EVENING' THEN 3
                       WHEN 'COMMUTE' THEN 4
                       WHEN 'OFFWORK' THEN 5
                       ELSE 6
                     END
                 ) AS rn
          FROM agg a
        )
        SELECT behavior, dow, timeSlot, cnt
        FROM ranked
        WHERE rn = 1
        ORDER BY behavior, dow
        """, nativeQuery = true)
    List<Object[]> findDominantPointsByItems(@Param("reportId") Long reportId);
}
