package com.smooth.driving_analysis_service.reports.behavior.controller;

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
        log.info("위험운전 행동 분석 API 호출 - reportId: {}", reportId);
        
        BehaviorAnalysisResponseDto analysis = service.getBehaviorAnalysis(reportId);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "code", "SUCCESS", 
                "message", "ok",
                "data", analysis
        ));
    }
}
