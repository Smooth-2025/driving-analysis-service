package com.smooth.driving_analysis_service.reports.behavior.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smooth.driving_analysis_service.global.auth.AuthenticationUtils;
import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.BehaviorAnalysisResponseDto;
import com.smooth.driving_analysis_service.reports.behavior.service.BehaviorReportService;
import com.smooth.driving_analysis_service.reports.common.service.MockDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/driving-analysis/reports")
@Slf4j
public class BehaviorReportController {
    private final ObjectMapper mapper = new ObjectMapper();
    private final BehaviorReportService service;

//    위험운전 행동 분석 API */
    @GetMapping("/{reportId}/behavior")
    public ResponseEntity<ApiResponse<BehaviorAnalysisResponseDto>> getBehaviorAnalysis(@PathVariable String reportId) {
        Long userId = AuthenticationUtils.getCurrentUserIdOrThrow();
        log.info("위험운전 행동 분석 API 호출 - reportId: {}", reportId);

        BehaviorAnalysisResponseDto analysis = service.getBehaviorAnalysis(reportId);

        log.info("위험운전 행동 분석 API 성공 - reportId: {}", reportId);
        return ResponseEntity.ok(ApiResponse.success("위험운전 행동 분석 조회가 완료되었습니다.", analysis));
    }

}
