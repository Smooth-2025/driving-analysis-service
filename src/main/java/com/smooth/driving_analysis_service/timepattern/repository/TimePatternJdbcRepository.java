// src/main/java/com/smooth/driving_analysis_service/timepattern/repository/TimePatternJdbcRepository.java
package com.smooth.driving_analysis_service.timepattern.repository;

import com.smooth.driving_analysis_service.timepattern.dto.TimePatternGridCellDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TimePatternJdbcRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // time_zone 테이블이 없으면 아래 SQL_UTC_PLUS9 사용
    private static final String SQL_CONVERT_TZ = """
        WITH lastN AS (
          SELECT *
          FROM (
            SELECT ts.*,
                   ROW_NUMBER() OVER (PARTITION BY ts.user_id ORDER BY ts.end_time DESC) AS rn
            FROM trip_summary ts
            WHERE ts.user_id = :userId
          ) t
          WHERE t.rn <= :limitN
        )
        SELECT
          (DAYOFWEEK(CONVERT_TZ(l.start_time,'+00:00','+09:00')) + 5) % 7 AS dow,
          HOUR(CONVERT_TZ(l.start_time,'+00:00','+09:00'))             AS hour,
          COUNT(*)                                                      AS trips,
          SUM(l.hard_brake_count)                                       AS hardBrakes,
          SUM(l.rapid_acceleration_count)                               AS rapidAccels,
          SUM(l.lane_change_count)                                      AS laneChanges
        FROM lastN l
        GROUP BY dow, hour
        ORDER BY dow, hour
        """;

    private static final String SQL_UTC_PLUS9 = """
        WITH lastN AS (
          SELECT *
          FROM (
            SELECT ts.*,
                   ROW_NUMBER() OVER (PARTITION BY ts.user_id ORDER BY ts.end_time DESC) AS rn
            FROM trip_summary ts
            WHERE ts.user_id = :userId
          ) t
          WHERE t.rn <= :limitN
        )
        SELECT
          (DAYOFWEEK(l.start_time + INTERVAL 9 HOUR) + 5) % 7 AS dow,
          HOUR(l.start_time + INTERVAL 9 HOUR)                AS hour,
          COUNT(*)                                            AS trips,
          SUM(l.hard_brake_count)                             AS hardBrakes,
          SUM(l.rapid_acceleration_count)                     AS rapidAccels,
          SUM(l.lane_change_count)                            AS laneChanges
        FROM lastN l
        GROUP BY dow, hour
        ORDER BY dow, hour
        """;

    public List<TimePatternGridCellDto> findTimeBinsForLastNTrips(String userId, int limitN, boolean useConvertTz) {
        var sql = useConvertTz ? SQL_CONVERT_TZ : SQL_UTC_PLUS9;
        var params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("limitN", limitN);

        return jdbc.query(sql, params, (rs, i) -> TimePatternGridCellDto.builder()
                .dow(rs.getInt("dow"))
                .hour(rs.getInt("hour"))
                .trips(rs.getLong("trips"))
                .hardBrakes(rs.getLong("hardBrakes"))
                .rapidAccels(rs.getLong("rapidAccels"))
                .laneChanges(rs.getLong("laneChanges"))
                .build());
    }
}
