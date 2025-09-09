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

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class AthenaMetricSourceImpl implements DnaMetricSource {

    private final AthenaClient athena;

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
