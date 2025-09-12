package com.smooth.driving_analysis_service.reports.basic_summary.controller;

import com.smooth.driving_analysis_service.global.auth.AuthenticationUtils;
import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.reports.basic_summary.dto.response.BasicSummaryResponseDto;
import com.smooth.driving_analysis_service.reports.basic_summary.service.BasicSummaryService;
import com.smooth.driving_analysis_service.reports.common.service.MockDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/driving-analysis/reports")
@RequiredArgsConstructor
public class BasicSummaryController {

    private final BasicSummaryService basicSummaryService;
    private final MockDataService mockDataService;

    /**
     * 리포트 상단 요약 조회
     */
    @GetMapping("/{reportId}/basic-summary")
    public ResponseEntity<ApiResponse<BasicSummaryResponseDto>> getBasicSummary(@PathVariable String reportId) {
        Long userId = AuthenticationUtils.getCurrentUserIdOrThrow();
        log.info("기본 정보 분석 API 호출 reportId={}, userId={}", reportId, userId);
        
        BasicSummaryResponseDto summary = basicSummaryService.getBasicSummaryByReportId(reportId, userId);


        return ResponseEntity.ok(ApiResponse.success("리포트_기본정보 요약 조회 완료", summary));
    }
}