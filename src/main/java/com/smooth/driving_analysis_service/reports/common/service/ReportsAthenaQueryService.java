package com.smooth.driving_analysis_service.reports.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.model.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;


/**
 * Reports 패키지에서 사용하는 Athena 쿼리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportsAthenaQueryService {
    
    private final AthenaClient athenaClient;
    
    @Value("${S3_OUTPUT:s3://bucket-of-smooth/athena-result/driving}")
    private String athenaOutputLocation;
    
    @Value("${athena.database:driving_analysis}")
    private String athenaDatabase;
    
    /**
     * Athena 쿼리 실행하고 결과를 Map 리스트로 반환
     */
    public List<Map<String, Object>> executeQuery(String query) {
        try {
            log.debug("Executing Athena query: {}", query);
            
            String queryExecutionId = startQueryExecution(query);
            waitForQueryCompletion(queryExecutionId);
            return getQueryResults(queryExecutionId);
            
        } catch (Exception e) {
            log.error("Failed to execute Athena query", e);
            throw new RuntimeException("Athena 쿼리 실행 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 쿼리 실행 시작
     */
    private String startQueryExecution(String query) {
        ResultConfiguration resultConfiguration = ResultConfiguration.builder()
                .outputLocation(athenaOutputLocation)
                .build();
                
        QueryExecutionContext queryExecutionContext = QueryExecutionContext.builder()
                .database(athenaDatabase)
                .build();
                
        StartQueryExecutionRequest request = StartQueryExecutionRequest.builder()
                .queryString(query)
                .workGroup("primary")
                .resultConfiguration(resultConfiguration)
                .queryExecutionContext(queryExecutionContext)
                .build();
        
        StartQueryExecutionResponse response = athenaClient.startQueryExecution(request);
        return response.queryExecutionId();
    }
    
    /**
     * 쿼리 완료 대기
     */
    private void waitForQueryCompletion(String queryExecutionId) {
        GetQueryExecutionRequest request = GetQueryExecutionRequest.builder()
                .queryExecutionId(queryExecutionId)
                .build();
        
        QueryExecutionState state;
        int maxAttempts = 60; // 최대 5분 대기
        int attempts = 0;
        
        do {
            try {
                Thread.sleep(5000); // 5초 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Query execution interrupted", e);
            }
            
            GetQueryExecutionResponse response = athenaClient.getQueryExecution(request);
            state = response.queryExecution().status().state();
            
            if (state == QueryExecutionState.FAILED || state == QueryExecutionState.CANCELLED) {
                String reason = response.queryExecution().status().stateChangeReason();
                throw new RuntimeException("Query failed: " + reason);
            }
            
            attempts++;
            if (attempts >= maxAttempts) {
                throw new RuntimeException("Query execution timeout");
            }
            
        } while (state == QueryExecutionState.QUEUED || state == QueryExecutionState.RUNNING);
    }
    
    /**
     * 쿼리 결과 조회
     */
    private List<Map<String, Object>> getQueryResults(String queryExecutionId) {
        GetQueryResultsRequest request = GetQueryResultsRequest.builder()
                .queryExecutionId(queryExecutionId)
                .build();
        
        GetQueryResultsResponse response = athenaClient.getQueryResults(request);
        List<Row> rows = response.resultSet().rows();
        
        if (rows.isEmpty()) {
            return List.of();
        }
        
        // 첫 번째 행은 헤더
        List<String> headers = rows.get(0).data().stream()
                .map(Datum::varCharValue)
                .toList();
        
        // 데이터 행들을 Map으로 변환
        return rows.stream()
                .skip(1) // 헤더 스킵
                .map(row -> {
                    Map<String, Object> rowMap = new HashMap<>();
                    List<Datum> data = row.data();
                    
                    for (int i = 0; i < headers.size() && i < data.size(); i++) {
                        String value = data.get(i).varCharValue();
                        rowMap.put(headers.get(i), value);
                    }
                    
                    return rowMap;
                })
                .toList();
    }
}