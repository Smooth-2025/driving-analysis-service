package com.smooth.driving_analysis_service.reports.behavior.repository;

import com.smooth.driving_analysis_service.driving.entity.DrivingRecord;

import com.smooth.driving_analysis_service.reports.behavior.dto.result.BehaviorSummaryResultDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.projection.BehaviorSummaryProjectionDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

//public interface TotalCountsProjection {
//    Long getHardBrake();
//    Long getRapidAccel();
//    Long getLaneChange();
//}

@Repository
public interface BehaviorDrivingRecordRepository extends JpaRepository<DrivingRecord, Long> {

  /**
   * totalCounts
   */
  @Query(value = """
      SELECT
          COALESCE(SUM(dac.hard_brake_count),0) as hardBrakeCount,
          COALESCE(SUM(dac.rapid_accel_count),0) as rapidAccelCount,
          COALESCE(SUM(dac.lane_change_count),0) as laneChangeCount
      FROM driving_record dac
      JOIN milestone_item mi ON mi.driving_id = dac.driving_id
      WHERE mi.report_id = :reportId
      """, nativeQuery = true)
  BehaviorSummaryProjectionDto fetchSummaryProjection(@Param("reportId") Long reportId);

//  // 기존 메서드 유지 (하위 호환성)
//  default BehaviorSummaryResultDto fetchSummary(Long reportId) {
//    BehaviorSummaryProjectionDto projection = fetchSummaryProjection(reportId);
//    return new BehaviorSummaryResultDto(
//        projection.getHardBrakeCount() != null ? projection.getHardBrakeCount() : 0,
//        projection.getRapidAccelCount() != null ? projection.getRapidAccelCount() : 0,
//        projection.getLaneChangeCount() != null ? projection.getLaneChangeCount() : 0);
//  }

  /**
   * 이전 report 기준 hardBrake, rapidAccel, laneChange 합계 조회
   */
  @Query(value = """
      SELECT
          COALESCE(SUM(dr.hard_brake_count), 0) as hardBrakeCount,
          COALESCE(SUM(dr.rapid_accel_count), 0) as rapidAccelCount,
          COALESCE(SUM(dr.lane_change_count), 0) as laneChangeCount
      FROM driving_record dr
      JOIN milestone_item mi ON mi.driving_id = dr.driving_id
      WHERE mi.report_id = :prevReportId
      """, nativeQuery = true)
  BehaviorSummaryProjectionDto fetchPrevSummaryProjection(@Param("prevReportId") Long prevReportId);

//  // 기존 메서드 유지 (하위 호환성)
//  default BehaviorSummaryResultDto fetchPrevSummary(Long prevReportId) {
//    BehaviorSummaryProjectionDto projection = fetchPrevSummaryProjection(prevReportId);
//    return new BehaviorSummaryResultDto(
//        projection.getHardBrakeCount() != null ? projection.getHardBrakeCount() : 0,
//        projection.getRapidAccelCount() != null ? projection.getRapidAccelCount() : 0,
//        projection.getLaneChangeCount() != null ? projection.getLaneChangeCount() : 0);
//  }

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
        FROM driving_record dr
        JOIN milestone_item mi ON mi.driving_id = dr.driving_id
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
        FROM driving_record dr
        JOIN milestone_item mi ON mi.driving_id = dr.driving_id
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
        FROM driving_record dr
        JOIN milestone_item mi ON mi.driving_id = dr.driving_id
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

  /**
   * String reportId 버전 - 새로운 API용 (reportId를 Long으로 변환)
   */
  @Query(value = """
      SELECT
          COALESCE(SUM(dr.hard_brake_count),0) as hardBrakeCount,
          COALESCE(SUM(dr.rapid_accel_count),0) as rapidAccelCount,
          COALESCE(SUM(dr.lane_change_count),0) as laneChangeCount
      FROM driving_record dr
      JOIN milestone_item mi ON mi.driving_id = dr.driving_id
      WHERE mi.report_id = :reportId
      """, nativeQuery = true)
  BehaviorSummaryProjectionDto fetchSummaryByStringIdProjection(@Param("reportId") Long reportId);

  // 기존 메서드 유지 (하위 호환성)
  default BehaviorSummaryResultDto fetchSummaryByStringId(Long reportId) {
    BehaviorSummaryProjectionDto projection = fetchSummaryByStringIdProjection(reportId);
    return new BehaviorSummaryResultDto(
        projection.getHardBrakeCount() != null ? projection.getHardBrakeCount() : 0,
        projection.getRapidAccelCount() != null ? projection.getRapidAccelCount() : 0,
        projection.getLaneChangeCount() != null ? projection.getLaneChangeCount() : 0);
  }

  /**
   * 요일별 시간대별 행동 집계 조회 (Long reportId 버전)
   */
  @Query(value = """
      WITH base AS (
        SELECT
          CASE DAYOFWEEK(dr.end_time)
            WHEN 1 THEN '일' WHEN 2 THEN '월' WHEN 3 THEN '화' WHEN 4 THEN '수'
            WHEN 5 THEN '목' WHEN 6 THEN '금' WHEN 7 THEN '토'
          END AS weekday,
          CASE
            WHEN HOUR(dr.end_time) < 6  THEN '새벽'
            WHEN HOUR(dr.end_time) < 10 THEN '출근'
            WHEN HOUR(dr.end_time) < 17 THEN '낮'
            WHEN HOUR(dr.end_time) < 20 THEN '퇴근'
            ELSE '저녁'
          END AS timeSlot,
          'HARD_BRAKE' AS behavior, dr.hard_brake_count AS cnt
        FROM driving_record dr
        JOIN milestone_item mi ON mi.driving_id = dr.driving_id
        WHERE mi.report_id = :reportId
        UNION ALL
        SELECT
          CASE DAYOFWEEK(dr.end_time)
            WHEN 1 THEN '일' WHEN 2 THEN '월' WHEN 3 THEN '화' WHEN 4 THEN '수'
            WHEN 5 THEN '목' WHEN 6 THEN '금' WHEN 7 THEN '토'
          END,
          CASE WHEN HOUR(dr.end_time) < 6 THEN '새벽'
               WHEN HOUR(dr.end_time) < 10 THEN '출근'
               WHEN HOUR(dr.end_time) < 17 THEN '낮'
               WHEN HOUR(dr.end_time) < 20 THEN '퇴근'
               ELSE '저녁' END,
          'RAPID_ACCEL', dr.rapid_accel_count
        FROM driving_record dr
        JOIN milestone_item mi ON mi.driving_id = dr.driving_id
        WHERE mi.report_id = :reportId
        UNION ALL
        SELECT
          CASE DAYOFWEEK(dr.end_time)
            WHEN 1 THEN '일' WHEN 2 THEN '월' WHEN 3 THEN '화' WHEN 4 THEN '수'
            WHEN 5 THEN '목' WHEN 6 THEN '금' WHEN 7 THEN '토'
          END,
          CASE WHEN HOUR(dr.end_time) < 6 THEN '새벽'
               WHEN HOUR(dr.end_time) < 10 THEN '출근'
               WHEN HOUR(dr.end_time) < 17 THEN '낮'
               WHEN HOUR(dr.end_time) < 20 THEN '퇴근'
               ELSE '저녁' END,
          'LANE_CHANGE', dr.lane_change_count
        FROM driving_record dr
        JOIN milestone_item mi ON mi.driving_id = dr.driving_id
        WHERE mi.report_id = :reportId
      ),
      agg AS (
        SELECT weekday, timeSlot, behavior, SUM(cnt) AS cnt
        FROM base
        GROUP BY weekday, timeSlot, behavior
      ),
      ranked AS (
        SELECT a.*,
               ROW_NUMBER() OVER (
                 PARTITION BY weekday, behavior
                 ORDER BY cnt DESC,
                   CASE timeSlot
                     WHEN '새벽' THEN 1
                     WHEN '낮'   THEN 2
                     WHEN '저녁' THEN 3
                     WHEN '출근' THEN 4
                     WHEN '퇴근' THEN 5
                     ELSE 6
                   END
               ) AS rn
        FROM agg a
      )
      SELECT weekday, behavior, timeSlot, cnt
      FROM ranked
      WHERE rn = 1
      ORDER BY
        CASE weekday WHEN '월' THEN 1 WHEN '화' THEN 2 WHEN '수' THEN 3 WHEN '목' THEN 4
                     WHEN '금' THEN 5 WHEN '토' THEN 6 WHEN '일' THEN 7 END,
        behavior
      """, nativeQuery = true)
  List<Object[]> findWeeklyDominantTimeSlots(@Param("reportId") Long reportId);
}
