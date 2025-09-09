package com.smooth.driving_analysis_service.reports.behavior.controller;

import com.smooth.driving_analysis_service.global.auth.AuthenticationUtils;
import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.BehaviorAnalysisResponseDto;
import com.smooth.driving_analysis_service.reports.behavior.service.BehaviorReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/driving-analysis/reports")
@Slf4j
public class BehaviorReportController {

    private final BehaviorReportService service;

    /** Task 1: totalCounts 구현 - 위험운전 행동 분석 API */
    @GetMapping("/{reportId}/behavior")
    public ResponseEntity<ApiResponse<BehaviorAnalysisResponseDto>> getBehaviorAnalysis(@PathVariable String reportId) {
        Long userId = AuthenticationUtils.getCurrentUserIdOrThrow();
        log.info("위험운전 행동 분석 API 호출 - reportId: {}, userId: {}", reportId, userId);

        BehaviorAnalysisResponseDto analysis = service.getBehaviorAnalysis(reportId);

        log.info("위험운전 행동 분석 API 성공 - reportId: {}, userId: {}", reportId, userId);
        return ResponseEntity.ok(ApiResponse.success("위험운전 행동 분석 조회가 완료되었습니다.", analysis));
    }
}
