package com.smooth.driving_analysis_service.driving.service;

import com.smooth.driving_analysis_service.driving.dto.result.DrivingAnalysisResultDto;
import com.smooth.driving_analysis_service.driving.dto.result.EventAnalysisResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class AthenaQueryServiceImpl implements AthenaQueryService {

    private final AthenaClient athenaClient;
    
    @Value("${athena.output-location}")
    private String athenaOutputLocation;
    
    @Value("${athena.database}")
    private String athenaDatabase;

    // 테이블명은 상수로 관리
    private static final String DRIVING_DATA_TABLE = "driving_data";
    private static final String EVENT_DATA_TABLE = "event_data";

    @Override
    public DrivingAnalysisResultDto getDrivingAnalysis(String drivingId) {

        String query = """
            SELECT 
                MAX(CASE WHEN event_type = 'driving_start' THEN timestamp END) as start_time,
                MAX(CASE WHEN event_type = 'driving_end' THEN timestamp END) as end_time,
                
                AVG(CASE WHEN event_type = 'driving_update' THEN speed END) as avg_speed,
                MAX(CASE WHEN event_type = 'driving_update' THEN speed END) as max_speed,
                MIN(CASE WHEN event_type = 'driving_update' THEN speed END) as min_speed,
                
                MAX(CASE WHEN event_type = 'driving_end' THEN total_distance END) as total_distance,
                
                -- 순항 비율 계산 (30-80km/h 구간 비율)
                COALESCE(
                    CAST(COUNT(CASE WHEN event_type = 'driving_update' AND speed BETWEEN 30 AND 80 THEN 1 END) AS DOUBLE) 
                    / NULLIF(COUNT(CASE WHEN event_type = 'driving_update' THEN 1 END), 0),
                    0.0
                ) as cruise_ratio
                
            FROM %s.%s
            WHERE driving_id = '%s'
            GROUP BY driving_id
            """.formatted(athenaDatabase, DRIVING_DATA_TABLE, drivingId);

        try {
            log.info("주행 분석 쿼리 실행: drivingId={}", drivingId);

            // 아테나 쿼리 실행
            String executionId = executeQuery(query);

            // 결과 대기 및 조회
            List<Map<String, Object>> results = getQueryResults(executionId);

            if (results.isEmpty()) {
                log.warn("주행 데이터를 찾을 수 없습니다: drivingId={}", drivingId);
                return createEmptyDrivingAnalysisResult();
            }

            Map<String, Object> result = results.get(0);
            
            // 아테나 쿼리 결과 로그 출력
            log.info("아테나 쿼리 결과: drivingId={}, result={}", drivingId, result);
            log.info("start_time 원본 값: {}", result.get("start_time"));
            log.info("end_time 원본 값: {}", result.get("end_time"));

            return DrivingAnalysisResultDto.builder()
                    .startTime(parseTimestamp(result.get("start_time")))
                    .endTime(parseTimestamp(result.get("end_time")))
                    .totalDistance(parseDouble(result.get("total_distance")))
                    .avgSpeed(parseDouble(result.get("avg_speed")))
                    .maxSpeed(parseDouble(result.get("max_speed")))
                    .minSpeed(parseDouble(result.get("min_speed")))
                    .cruiseRatio(parseDouble(result.get("cruise_ratio")))
                    .build();

        } catch (Exception e) {
            log.error("주행 분석 쿼리 실행 중 오류 발생: drivingId={}", drivingId, e);
            throw new RuntimeException("주행 데이터 분석 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public EventAnalysisResultDto getEventAnalysis(String drivingId) {

        String query = """
                SELECT
                    COUNT(CASE WHEN eventType = 'lane_change' THEN 1 END) as lane_change_count,
                    COUNT(CASE WHEN eventType = 'hard_brake' THEN 1 END) as hard_brake_count,
                    COUNT(CASE WHEN eventType = 'rapid_accel' THEN 1 END) as rapid_accel_count,
                    COUNT(CASE WHEN eventType = 'sharp_turn' THEN 1 END) as sharp_turn_count
            FROM %s.%s
            WHERE driving_id = '%s'
            """.formatted(athenaDatabase, EVENT_DATA_TABLE, drivingId);

        try {
            log.info("이벤트 분석 쿼리 실행: drivingId={}", drivingId);

            String executionId = executeQuery(query);
            List<Map<String, Object>> results = getQueryResults(executionId);

            if (results.isEmpty()) {
                log.warn("이벤트 데이터를 찾을 수 없습니다: drivingId={}", drivingId);
                return createEmptyEventAnalysisResult();
            }

            Map<String, Object> result = results.get(0);

            return EventAnalysisResultDto.builder()
                    .laneChangeCount(parseInt(result.get("lane_change_count")))
                    .hardBrakeCount(parseInt(result.get("hard_brake_count")))
                    .rapidAccelCount(parseInt(result.get("rapid_accel_count")))
                    .sharpTurnCount(parseInt(result.get("sharp_turn_count")))
                    .build();

        } catch (Exception e) {
            log.error("이벤트 분석 쿼리 실행 중 오류 발생: drivingId={}", drivingId, e);
            throw new RuntimeException("이벤트 데이터 분석 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 아테나 쿼리 실행
     */
    private String executeQuery(String query) {
        ResultConfiguration resultConfiguration = ResultConfiguration.builder()
                .outputLocation(athenaOutputLocation)
                .build();
                
        StartQueryExecutionRequest request = StartQueryExecutionRequest.builder()
                .queryString(query)
                .workGroup("primary") // 워크그룹 설정
                .resultConfiguration(resultConfiguration)
                .build();

        StartQueryExecutionResponse response = athenaClient.startQueryExecution(request);
        return response.queryExecutionId();
    }

    /**
     * 쿼리 결과 조회 (폴링)
     */
    private List<Map<String, Object>> getQueryResults(String executionId) throws InterruptedException {

        // 쿼리 완료까지 대기
        waitForQueryCompletion(executionId);

        // 결과 조회
        GetQueryResultsRequest request = GetQueryResultsRequest.builder()
                .queryExecutionId(executionId)
                .build();

        GetQueryResultsResponse response = athenaClient.getQueryResults(request);

        return parseQueryResults(response);
    }

    /**
     * 쿼리 완료까지 대기
     */
    private void waitForQueryCompletion(String executionId) throws InterruptedException {
        GetQueryExecutionRequest request = GetQueryExecutionRequest.builder()
                .queryExecutionId(executionId)
                .build();

        QueryExecutionState state;
        do {
            Thread.sleep(1000); // 1초 대기

            GetQueryExecutionResponse response = athenaClient.getQueryExecution(request);
            state = response.queryExecution().status().state();

            log.debug("쿼리 실행 상태: executionId={}, state={}", executionId, state);

            if (state == QueryExecutionState.FAILED || state == QueryExecutionState.CANCELLED) {
                String reason = response.queryExecution().status().stateChangeReason();
                throw new RuntimeException("쿼리 실행 실패: " + reason);
            }

        } while (state == QueryExecutionState.RUNNING || state == QueryExecutionState.QUEUED);
    }


    private List<Map<String, Object>> parseQueryResults(GetQueryResultsResponse response) {
        List<Map<String, Object>> results = new ArrayList<>();

        List<Row> rows = response.resultSet().rows();
        if (rows.size() <= 1) return results;

        List<String> headers = rows.get(0).data().stream()
                .map(Datum::varCharValue)
                .toList();

        for (int i = 1; i < rows.size(); i++) {
            Row row = rows.get(i);
            Map<String, Object> resultMap = new HashMap<>();

            for (int j = 0; j < headers.size(); j++) {
                String header = headers.get(j);
                String value = row.data().get(j).varCharValue();
                resultMap.put(header, value);
            }

            results.add(resultMap);
        }

        return results;
    }

    // 유틸리티 메소드들
    private LocalDateTime parseTimestamp(Object value) {
        if (value == null) return null;
        try {
            return LocalDateTime.parse(value.toString().substring(0, 19));
        } catch (Exception e) {
            log.warn("타임스탬프 파싱 실패: {}", value, e);
            return null;
        }
    }

    private Double parseDouble(Object value) {
        if (value == null) return 0.0;
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            log.warn("Double 파싱 실패: {}", value, e);
            return 0.0;
        }
    }

    private Integer parseInt(Object value) {
        if (value == null) return 0;
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            log.warn("Integer 파싱 실패: {}", value, e);
            return 0;
        }
    }

    private DrivingAnalysisResultDto createEmptyDrivingAnalysisResult() {
        return DrivingAnalysisResultDto.builder()
                .totalDistance(0.0)
                .avgSpeed(0.0)
                .maxSpeed(0.0)
                .minSpeed(0.0)
                .cruiseRatio(0.0)
                .build();
    }

    private EventAnalysisResultDto createEmptyEventAnalysisResult() {
        return EventAnalysisResultDto.builder()
                .laneChangeCount(0)
                .hardBrakeCount(0)
                .rapidAccelCount(0)
                .sharpTurnCount(0)
                .build();
    }
}
