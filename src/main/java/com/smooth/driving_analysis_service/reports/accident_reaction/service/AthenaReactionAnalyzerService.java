package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import com.smooth.driving_analysis_service.reports.accident_reaction.support.DrivingEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.model.*;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class AthenaReactionAnalyzerService {

    private final AthenaClient athenaClient;

    @Value("${athena.database}")
    private String database;

    @Value("${athena.output-location}")
    private String outputLocation;

    // 있으면 사용, 없으면 기본 event_data
    @Value("${accident.reaction.athena.table:event_data}")
    private String table;

    @Value("${athena.workgroup:primary}")
    private String workgroup;

    /**
     * drivingId가 있는 '타이트' 조회: 주어진 윈도우에서 가장 이른 이벤트 1건
     */
    public Optional<FoundEvent> findEarliestEvent(long userId, String drivingId, Instant from, Instant to) {
        String sql = """
            SELECT
                event_type,
                driving_id,
                from_iso8601_timestamp(event_ts) AS ts
            FROM %s.%s
            WHERE user_id = %d
              AND driving_id = '%s'
              AND from_iso8601_timestamp(event_ts)
                  BETWEEN from_iso8601_timestamp('%s') AND from_iso8601_timestamp('%s')
            ORDER BY ts ASC
            LIMIT 1
        """.formatted(database, table, userId, esc(drivingId),
                DateTimeFormatter.ISO_INSTANT.format(from),
                DateTimeFormatter.ISO_INSTANT.format(to));

        return executeSingle(sql);
    }

    /**
     * drivingId 없이 '루즈' 조회: 주어진 윈도우에서 가장 이른 이벤트 1건
     */
    public Optional<FoundEvent> findEarliestEventLoose(long userId, Instant from, Instant to) {
        String sql = """
            SELECT
                event_type,
                driving_id,
                from_iso8601_timestamp(event_ts) AS ts
            FROM %s.%s
            WHERE user_id = %d
              AND from_iso8601_timestamp(event_ts)
                  BETWEEN from_iso8601_timestamp('%s') AND from_iso8601_timestamp('%s')
            ORDER BY ts ASC
            LIMIT 1
        """.formatted(database, table, userId,
                DateTimeFormatter.ISO_INSTANT.format(from),
                DateTimeFormatter.ISO_INSTANT.format(to));

        return executeSingle(sql);
    }

    /* ======================== 내부 공통 루틴 (네 Impl 스타일) ======================== */

    private Optional<FoundEvent> executeSingle(String sql) {
        try {
            log.info("[사고반응][Athena] 쿼리 실행: workgroup={}, database={}, table={}", workgroup, database, table);
            String executionId = startQuery(sql);

            // 완료 대기
            waitForQueryCompletion(executionId);

            // 결과 조회
            GetQueryResultsResponse res = athenaClient.getQueryResults(GetQueryResultsRequest.builder()
                    .queryExecutionId(executionId)
                    .build());

            List<Row> rows = res.resultSet().rows();
            if (rows == null || rows.size() <= 1) {
                log.info("[사고반응][Athena] 결과 없음(헤더만 존재): executionId={}", executionId);
                return Optional.empty();
            }

            // 헤더
            List<String> headers = rows.get(0).data().stream().map(Datum::varCharValue).toList();

            // 첫 번째 데이터 로우
            Row r = rows.get(1);
            Map<String, String> rowMap = new HashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                rowMap.put(headers.get(i), r.data().get(i).varCharValue());
            }

            String eventTypeStr = rowMap.getOrDefault("event_type", null);
            String drivingIdStr = rowMap.getOrDefault("driving_id", null);
            String tsStr        = rowMap.getOrDefault("ts", null);

            Instant ts = parseAthenaInstant(tsStr);

            log.info("[사고반응][Athena] 쿼리 성공: executionId={}, eventType={}, drivingId={}, ts={}",
                    executionId, eventTypeStr, drivingIdStr, ts);
            return Optional.of(new FoundEvent(DrivingEventType.of(eventTypeStr), drivingIdStr, ts));

        } catch (Exception e) {
            log.error("[사고반응][Athena] 쿼리 실행 실패: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    private String startQuery(String query) {
        ResultConfiguration resultConfiguration = ResultConfiguration.builder()
                .outputLocation(outputLocation)
                .build();

        StartQueryExecutionRequest request = StartQueryExecutionRequest.builder()
                .queryString(query)
                .workGroup(workgroup)
                .queryExecutionContext(QueryExecutionContext.builder().database(database).build())
                .resultConfiguration(resultConfiguration)
                .build();

        StartQueryExecutionResponse response = athenaClient.startQueryExecution(request);
        return response.queryExecutionId();
    }

    private void waitForQueryCompletion(String executionId) throws InterruptedException {
        GetQueryExecutionRequest request = GetQueryExecutionRequest.builder()
                .queryExecutionId(executionId)
                .build();

        QueryExecutionState state;
        do {
            Thread.sleep(800); // Athena 특성상 0.4s보단 약간 여유
            GetQueryExecutionResponse response = athenaClient.getQueryExecution(request);
            state = response.queryExecution().status().state();
            log.debug("[사고반응][Athena] 실행 상태: executionId={}, state={}", executionId, state);

            if (state == QueryExecutionState.FAILED || state == QueryExecutionState.CANCELLED) {
                String reason = response.queryExecution().status().stateChangeReason();
                throw new RuntimeException("Athena 쿼리 실패: " + reason);
            }

        } while (state == QueryExecutionState.RUNNING || state == QueryExecutionState.QUEUED);
    }

    private static String esc(String s) {
        return s.replace("'", "''");
    }

    /**
     * Athena가 반환하는 타임스탬프 문자열 안전 파싱
     * - 예: "2025-08-20 05:10:22.000 UTC" → "2025-08-20T05:10:22.000Z"
     * - ISO 인풋도 그대로 처리
     */
    private static Instant parseAthenaInstant(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            // ISO 형태면 바로
            return Instant.parse(raw);
        } catch (Exception ignore) {
        }
        try {
            // "YYYY-MM-DD HH:MM:SS(.SSS) UTC" → ISO로 치환
            String isoLike = raw.replace(' ', 'T').replace(" UTC", "Z");
            return Instant.parse(isoLike);
        } catch (Exception e) {
            log.warn("[사고반응][Athena] 타임스탬프 파싱 실패: {}", raw, e);
            return null;
        }
    }

    /** 결과 모델 (그대로 유지) */
    public record FoundEvent(DrivingEventType type, String drivingId, Instant eventInstant) {}
}
