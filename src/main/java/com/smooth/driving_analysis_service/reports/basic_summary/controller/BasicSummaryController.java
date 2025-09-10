package com.smooth.driving_analysis_service.reports.basic_summary.controller;

import com.smooth.driving_analysis_service.global.auth.AuthenticationUtils;
import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.global.exception.BusinessException;
import com.smooth.driving_analysis_service.global.exception.CommonErrorCode;
import com.smooth.driving_analysis_service.reports.basic_summary.dto.BasicSummaryResponseDto;
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
        
        BasicSummaryResponseDto summary = basicSummaryService.getBasicSummary(reportId);


        return ResponseEntity.ok(ApiResponse.success("리포트 상단 요약 조회 완료", summary));
    }

    /**
     * 기본 요약 목데이터 조회 API
     */
//    @GetMapping("/{reportId}/basic-summary/mock")
//    public ResponseEntity<ApiResponse<BasicSummaryResponseDto>> getBasicSummaryMockData(@PathVariable String reportId) {
//        BasicSummaryResponseDto mockData = BasicSummaryResponseDto.builder()
//                .reportId(reportId)
//                .totalDistanceKm(245.8)
//                .periodStart(java.time.LocalDate.of(2025, 8, 26))
//                .periodEnd(java.time.LocalDate.of(2025, 8, 31))
//                .averageDurationSec(1850.0)  // 약 30분
//                .averageDistanceKm(12.3)
//                .averageSpeedKmh(45.2)
//                .averageCruiseRatio(78)
//                .build();
//
//        return ResponseEntity.ok(ApiResponse.success("기본 요약 목데이터 조회 완료", mockData));
//    }
}