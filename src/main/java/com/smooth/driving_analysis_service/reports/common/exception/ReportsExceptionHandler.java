package com.smooth.driving_analysis_service.reports.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice(basePackages = "com.smooth.driving_analysis_service.reports")
@Order(1) // GlobalExceptionHandler보다 우선순위를 높게 설정
public class ReportsExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleReportsRuntimeException(RuntimeException e) {
        log.error("Reports module RuntimeException occurred: {}", e.getMessage(), e);

        // Athena 쿼리 관련 오류 특별 처리
        if (e.getMessage() != null && e.getMessage().contains("COLUMN_NOT_FOUND")) {
            log.error("Athena column not found error in reports: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "code", "ATHENA_QUERY_ERROR",
                    "message", "데이터 조회 중 스키마 오류가 발생했습니다. 관리자에게 문의하세요.",
                    "data", null
            ));
        }

        // Athena 쿼리 실행 실패
        if (e.getMessage() != null && e.getMessage().contains("Athena 쿼리 실행 실패")) {
            log.error("Athena query execution failed in reports: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "code", "ATHENA_EXECUTION_ERROR", 
                    "message", "데이터 조회 서비스에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요.",
                    "data", null
            ));
        }

        // 일반적인 Reports 모듈 오류
        return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "code", "REPORTS_ERROR",
                "message", "리포트 처리 중 오류가 발생했습니다: " + e.getMessage(),
                "data", null
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleReportsException(Exception e) {
        log.error("Reports module Exception occurred: {}", e.getMessage(), e);
        
        return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "code", "INTERNAL_SERVER_ERROR",
                "message", "리포트 서비스에서 예상치 못한 오류가 발생했습니다.",
                "data", null
        ));
    }
}