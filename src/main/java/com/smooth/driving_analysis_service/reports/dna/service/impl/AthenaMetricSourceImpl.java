package com.smooth.driving_analysis_service.reports.dna.service.impl;

import com.smooth.driving_analysis_service.reports.dna.service.DnaMetricSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.model.*;

import java.util.*;
import java.util.stream.Collectors;
import com.smooth.driving_analysis_service.reports.common.service.ReportsAthenaQueryService;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class AthenaMetricSourceImpl implements DnaMetricSource {

    private final AthenaClient athena;
    private final ReportsAthenaQueryService athenaQueryService;

    @Value("${athena.database}") private String database;
    @Value("${athena.workgroup:primary}") private String workgroup;
    @Value("${S3_OUTPUT:s3://bucket-of-smooth/athena-result/driving}") private String outputLocation;

    @Override
    public DnaInput loadForReport(Long reportId, List<String> drivingIds) {
        if (drivingIds == null || drivingIds.isEmpty()) return new DnaInput(List.of());

        String in = drivingIds.stream().map(id -> "'" + id + "'").collect(Collectors.joining(","));

        Map<String, Double> a0_40 = queryDoubleMap("""
            WITH t0 AS (
              SELECT driving_id, MIN(ts) AS t0
              FROM raw_driving_update
              WHERE driving_id IN (%s)
              GROUP BY driving_id
            ),
            t40 AS (
              SELECT driving_id, MIN(ts) AS t40
              FROM raw_driving_update
              WHERE driving_id IN (%s) AND speed_kmh >= 40
              GROUP BY driving_id
            )
            SELECT a.driving_id,
                   (unix_timestamp(b.t40) - unix_timestamp(a.t0)) AS sec_0_40
            FROM t0 a JOIN t40 b ON a.driving_id=b.driving_id
            """.formatted(in, in), "driving_id", "sec_0_40");

        Map<String, Double> decel = queryDoubleMap("""
            WITH sorted AS (
              SELECT driving_id, ts, speed_mps
              FROM raw_driving_update
              WHERE driving_id IN (%s)
            ),
            diff AS (
              SELECT driving_id,
                     (LEAD(speed_mps) OVER (PARTITION BY driving_id ORDER BY ts) - speed_mps)
                     / NULLIF(unix_timestamp(LEAD(ts) OVER (PARTITION BY driving_id ORDER BY ts))
                           - unix_timestamp(ts), 0) AS dvdt
              FROM sorted
            ),
            decel AS (
              SELECT driving_id, dvdt FROM diff
              WHERE dvdt IS NOT NULL AND dvdt < 0
            )
            SELECT driving_id, ABS(AVG(dvdt)) AS avg_decel_rate
            FROM decel GROUP BY driving_id
            """.formatted(in), "driving_id", "avg_decel_rate");

        Map<String, Double> lanePerKm = queryDoubleMap("""
            WITH L AS (
              SELECT driving_id, ts, x, y
              FROM raw_driving_update
              WHERE driving_id IN (%s)
            ),
            move AS (
              SELECT driving_id, ts,
                     ABS(y - LAG(y) OVER (PARTITION BY driving_id ORDER BY ts)) AS dy,
                     (unix_timestamp(ts)-unix_timestamp(LAG(ts) OVER (PARTITION BY driving_id ORDER BY ts))) AS dt
              FROM L
            ),
            lane_change AS (
              SELECT driving_id, COUNT(*) AS lc
              FROM move
              WHERE dy >= 3.2 AND dt <= 2.5
              GROUP BY driving_id
            ),
            dist AS (
              SELECT driving_id, SUM(segment_km) AS km
              FROM raw_driving_distance
              WHERE driving_id IN (%s)
              GROUP BY driving_id
            )
            SELECT d.driving_id, (lc.lc / NULLIF(d.km,0)) AS lc_per_km
            FROM dist d LEFT JOIN lane_change lc ON d.driving_id=lc.driving_id
            """.formatted(in, in), "driving_id", "lc_per_km");

        Map<String, Double> postAccel = queryDoubleMap("""
            WITH L AS (
              SELECT driving_id, ts, x, y, speed_mps
              FROM raw_driving_update
              WHERE driving_id IN (%s)
            ),
            move AS (
              SELECT driving_id, ts,
                     ABS(y - LAG(y) OVER (PARTITION BY driving_id ORDER BY ts)) AS dy,
                     (unix_timestamp(ts)-unix_timestamp(LAG(ts) OVER (PARTITION BY driving_id ORDER BY ts))) AS dt,
                     speed_mps
              FROM L
            ),
            lc AS (
              SELECT driving_id, ts AS t_lc
              FROM move
              WHERE dy >= 3.2 AND dt <= 2.5
            ),
            win AS (
              SELECT u.driving_id, l.t_lc,
                     AVG( (LEAD(u.speed_mps) OVER (PARTITION BY u.driving_id ORDER BY u.ts) - u.speed_mps)
                          / NULLIF(unix_timestamp(LEAD(u.ts) OVER (PARTITION BY u.driving_id ORDER BY u.ts))
                                   - unix_timestamp(u.ts),0) ) AS a_0_5
              FROM raw_driving_update u
              JOIN lc l ON u.driving_id=l.driving_id
              WHERE u.ts BETWEEN l.t_lc AND from_unixtime(unix_timestamp(l.t_lc)+5)
              GROUP BY u.driving_id, l.t_lc
            )
            SELECT driving_id, AVG(a_0_5) AS post_accel
            FROM win GROUP BY driving_id
            """.formatted(in), "driving_id", "post_accel");

        Map<String, Double> distanceKm = queryDoubleMap("""
            SELECT driving_id, SUM(segment_km) AS km
            FROM raw_driving_distance
            WHERE driving_id IN (%s)
            GROUP BY driving_id
            """.formatted(in), "driving_id", "km");

        List<PerDriving> list = new ArrayList<>();
        for (String id : drivingIds) {
            list.add(new PerDriving(
                    id,
                    a0_40.get(id),
                    decel.get(id),
                    lanePerKm.get(id),
                    postAccel.get(id),
                    distanceKm.getOrDefault(id, 0.0)
            ));
        }
        return new DnaInput(list);
    }

    @Override
    public Map<String, Double> getMetrics(Long reportId) {
        // 기존 방식 - reportId 기반
        log.info("Getting DNA metrics for reportId: {}", reportId);
        return Map.of(
            "safe_driving_score", 75.0,
            "eco_driving_score", 68.0,
            "defensive_driving_score", 82.0,
            "smooth_driving_score", 71.0
        );
    }
    
    @Override
    public Map<String, Double> getMetricsByDrivingIds(List<String> drivingIds) {
        // 새로운 방식 - drivingIds 기반
        log.info("Getting DNA metrics for {} driving records", drivingIds.size());
        
        // Athena 쿼리로 실제 DNA 4축 분석
        try {
            return executeDnaAnalysisQuery(drivingIds);
        } catch (Exception e) {
            log.error("DNA Athena 쿼리 실패, 기본값 사용", e);
            return Map.of(
                "safe_driving_score", 75.0,
                "eco_driving_score", 68.0,
                "defensive_driving_score", 82.0,
                "smooth_driving_score", 71.0
            );
        }
    }

    /**
     * DNA 4축 분석 쿼리 실행
     */
    private Map<String, Double> executeDnaAnalysisQuery(List<String> drivingIds) {
        String tripIds = drivingIds.stream()
                .map(id -> "'" + id + "'")
                .collect(Collectors.joining(","));
        
        // A축: 출발 성향 (0→40km/h 도달시간)
        double aAxisScore = calculateAAxisScore(tripIds);
        
        // B축: 감속 성향 (평균 감속률)  
        double bAxisScore = calculateBAxisScore(tripIds);
        
        // C축: 차선 변경 (km당 차선변경 횟수)
        double cAxisScore = calculateCAxisScore(tripIds);
        
        // D축: 사고 대응 (반응시간 & 액션)
        double dAxisScore = calculateDAxisScore(tripIds);
        
        return Map.of(
            "safe_driving_score", aAxisScore,      // A축 → 안전운전
            "eco_driving_score", bAxisScore,       // B축 → 경제운전  
            "defensive_driving_score", cAxisScore, // C축 → 방어운전
            "smooth_driving_score", dAxisScore     // D축 → 부드러운운전
        );
    }
    
    private double calculateAAxisScore(String tripIds) {
        String query = String.format("""
            WITH s AS (
              SELECT tripId,
                     from_timezone("timestamp",'Asia/Seoul') AS ts,
                     speed
              FROM raw_driving_message
              WHERE dt >= date_format(current_date - interval '30' day, '%%Y-%%m-%%d')
                AND tripId IN (%s)
            ),
            mark AS (
              SELECT 
                tripId,
                min(CASE WHEN speed < 1 THEN ts END) AS t0,
                min(CASE WHEN speed >= 40 THEN ts END) AS t40
              FROM s
              GROUP BY tripId
            )
            SELECT 
              avg(date_diff('second', t0, t40)) AS avg_accel_time
            FROM mark
            WHERE t0 IS NOT NULL AND t40 IS NOT NULL
            """, tripIds);
            
        try {
            List<Map<String, Object>> results = athenaQueryService.executeQuery(query);
            if (!results.isEmpty()) {
                Object avgTime = results.get(0).get("avg_accel_time");
                if (avgTime != null) {
                    double seconds = Double.parseDouble(avgTime.toString());
                    // 5초 이하=90점, 10초 이상=60점, 선형 보간
                    return Math.max(60.0, Math.min(90.0, 90.0 - (seconds - 5.0) * 6.0));
                }
            }
        } catch (Exception e) {
            log.error("A축 계산 실패", e);
        }
        return 75.0; // 기본값
    }
    
    private double calculateBAxisScore(String tripIds) {
        String query = String.format("""
            WITH s AS (
              SELECT tripId,
                     speed,
                     lag(speed) OVER (PARTITION BY tripId ORDER BY from_timezone("timestamp",'Asia/Seoul')) AS prev_speed,
                     unix_timestamp(from_timezone("timestamp",'Asia/Seoul')) AS tsec,
                     lag(unix_timestamp(from_timezone("timestamp",'Asia/Seoul')))
                       OVER (PARTITION BY tripId ORDER BY from_timezone("timestamp",'Asia/Seoul')) AS prev_tsec
              FROM raw_driving_message
              WHERE dt >= date_format(current_date - interval '30' day, '%%Y-%%m-%%d')
                AND tripId IN (%s)
            ),
            decel AS (
              SELECT tripId,
                     GREATEST(prev_speed - speed, 0) AS decel_speed,
                     NULLIF(tsec - prev_tsec,0) AS dt_sec
              FROM s
              WHERE prev_speed IS NOT NULL
            )
            SELECT 
              avg((decel_speed/3.6) / dt_sec) AS avg_decel_mps2
            FROM decel 
            WHERE dt_sec IS NOT NULL AND dt_sec > 0
            """, tripIds);
            
        try {
            List<Map<String, Object>> results = athenaQueryService.executeQuery(query);
            if (!results.isEmpty()) {
                Object avgDecel = results.get(0).get("avg_decel_mps2");
                if (avgDecel != null) {
                    double decelMps2 = Double.parseDouble(avgDecel.toString());
                    // 0.1m/s²=90점, 0.3m/s²=60점, 선형 보간
                    return Math.max(60.0, Math.min(90.0, 90.0 - (decelMps2 - 0.1) * 150.0));
                }
            }
        } catch (Exception e) {
            log.error("B축 계산 실패", e);
        }
        return 68.0; // 기본값
    }
    
    private double calculateCAxisScore(String tripIds) {
        String query = String.format("""
            WITH ev AS (
              SELECT tripId, count_if(eventType='lane_change') AS lane_changes
              FROM raw_report_message
              WHERE dt >= date_format(current_date - interval '30' day, '%%Y-%%m-%%d')
                AND tripId IN (%s)
              GROUP BY tripId
            ),
            dist AS (
              SELECT tripId,
                     sum(sqrt(power(locationX - lag(locationX) OVER (PARTITION BY tripId ORDER BY from_timezone("timestamp",'Asia/Seoul')),2)
                            + power(locationY - lag(locationY) OVER (PARTITION BY tripId ORDER BY from_timezone("timestamp",'Asia/Seoul')),2))) / 1000.0 AS distance_km
              FROM raw_driving_message
              WHERE dt >= date_format(current_date - interval '30' day, '%%Y-%%m-%%d')
                AND tripId IN (%s)
              GROUP BY tripId
            )
            SELECT 
              avg(e.lane_changes / NULLIF(d.distance_km,0)) AS avg_lane_change_per_km
            FROM ev e 
            JOIN dist d ON e.tripId=d.tripId
            WHERE d.distance_km > 0
            """, tripIds, tripIds);
            
        try {
            List<Map<String, Object>> results = athenaQueryService.executeQuery(query);
            if (!results.isEmpty()) {
                Object avgLaneChange = results.get(0).get("avg_lane_change_per_km");
                if (avgLaneChange != null) {
                    double laneChangePerKm = Double.parseDouble(avgLaneChange.toString());
                    // 1.0회/km=90점, 2.0회/km=60점, 선형 보간
                    return Math.max(60.0, Math.min(90.0, 90.0 - (laneChangePerKm - 1.0) * 30.0));
                }
            }
        } catch (Exception e) {
            log.error("C축 계산 실패", e);
        }
        return 82.0; // 기본값
    }
    
    private double calculateDAxisScore(String tripIds) {
        // D축은 알림 기반이므로 기본값 반환 (실제로는 알림 데이터 필요)
        log.info("D축은 알림 기반 분석이므로 기본값 사용");
        return 71.0;
    }

    // ===== Athena 공통 유틸 =====
    private Map<String, Double> queryDoubleMap(String sql, String keyCol, String valCol) {
        String qid = athena.startQueryExecution(StartQueryExecutionRequest.builder()
                .queryString(sql)
                .workGroup(workgroup)
                .queryExecutionContext(QueryExecutionContext.builder().database(database).build())
                .resultConfiguration(ResultConfiguration.builder()
                        .outputLocation(outputLocation)
                        .build())
                .build()).queryExecutionId();

        waitUntilSucceeded(qid);

        GetQueryResultsResponse res = athena.getQueryResults(GetQueryResultsRequest.builder()
                .queryExecutionId(qid).build());

        Map<String, Integer> idx = new HashMap<>();
        var cols = res.resultSet().resultSetMetadata().columnInfo();
        for (int i = 0; i < cols.size(); i++) idx.put(cols.get(i).name(), i);

        Map<String, Double> out = new HashMap<>();
        var rows = res.resultSet().rows();
        for (int i = 1; i < rows.size(); i++) { // 0: header
            var data = rows.get(i).data();
            String key = data.get(idx.get(keyCol)).varCharValue();
            String val = data.get(idx.get(valCol)).varCharValue();
            if (key != null) out.put(key, val == null || val.isBlank() ? null : Double.valueOf(val));
        }
        return out;
    }

    private void waitUntilSucceeded(String qid) {
        while (true) {
            var queryExecution = athena.getQueryExecution(GetQueryExecutionRequest.builder().queryExecutionId(qid).build())
                    .queryExecution();
            var st = queryExecution.status().state();
            if (st == QueryExecutionState.SUCCEEDED) return;
            if (st == QueryExecutionState.FAILED || st == QueryExecutionState.CANCELLED) {
                String errorMessage = queryExecution.status().stateChangeReason();
                log.error("Athena query failed: queryId={}, state={}, reason={}", qid, st, errorMessage);
                throw new RuntimeException("Athena failed: " + st + " - " + errorMessage);
            }
            try { Thread.sleep(600); } catch (InterruptedException ignored) {}
        }
    }
}