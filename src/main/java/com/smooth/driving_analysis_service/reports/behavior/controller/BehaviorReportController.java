package com.smooth.driving_analysis_service.reports.behavior.controller;

import com.smooth.driving_analysis_service.global.auth.AuthenticationUtils;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.BehaviorAnalysisResponseDto;
import com.smooth.driving_analysis_service.reports.behavior.service.BehaviorReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/driving-analysis/reports")
@Slf4j
public class BehaviorReportController {

    private final BehaviorReportService service;

    /** Task 1: totalCounts 구현 - 위험운전 행동 분석 API */
    @GetMapping("/{reportId}/behavior")
    public ResponseEntity<?> getBehaviorAnalysis(@PathVariable String reportId) {
        try {
            Long userId = AuthenticationUtils.getCurrentUserIdOrThrow();
            log.info("위험운전 행동 분석 API 호출 - reportId: {}, userId: {}", reportId, userId);
            
            BehaviorAnalysisResponseDto analysis = service.getBehaviorAnalysis(reportId);
            
            log.info("위험운전 행동 분석 API 성공 - reportId: {}, userId: {}", reportId, userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "code", "SUCCESS", 
                    "message", "ok",
                    "data", analysis
            ));
        } catch (RuntimeException e) {
            log.error("위험운전 행동 분석 API 실패 - reportId: {}", reportId, e);
            
            // Athena 관련 오류는 더 자세한 메시지 제공
            if (e.getMessage() != null && e.getMessage().contains("COLUMN_NOT_FOUND")) {
                log.error("Athena column not found error: {}", e.getMessage());
                throw new RuntimeException("데이터 조회 중 스키마 오류가 발생했습니다. 관리자에게 문의하세요.");
            }
            
            // 다른 RuntimeException은 그대로 전파 (GlobalExceptionHandler가 처리)
            throw e;
        }
    }
}
