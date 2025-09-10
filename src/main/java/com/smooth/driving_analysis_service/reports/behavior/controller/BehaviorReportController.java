package com.smooth.driving_analysis_service.reports.behavior.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.reports.exception.ReportErrorCode;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.BehaviorAnalysisResponseDto;
import com.smooth.driving_analysis_service.reports.behavior.service.BehaviorReportService;
import com.smooth.driving_analysis_service.reports.common.service.MockDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/driving-analysis/reports")
@Slf4j
public class BehaviorReportController {
    private final ObjectMapper mapper = new ObjectMapper();
    private final BehaviorReportService service;
    private final MockDataService mockDataService;

    /** Task 1: totalCounts 구현 - 위험운전 행동 분석 API */
    @GetMapping("/{reportId}/behavior")


    public ResponseEntity<ApiResponse<BehaviorAnalysisResponseDto>> getBehaviorAnalysis(
            @PathVariable String reportId
    ) {
        try (var is = new ClassPathResource("mock/behavior.json").getInputStream()) {
            BehaviorAnalysisResponseDto dto = mapper.readValue(is, BehaviorAnalysisResponseDto.class);
            return ResponseEntity.ok(ApiResponse.success("위험운전 행동 분석 조회가 완료되었습니다.", dto));
        } catch (IOException e) {
            log.error("mock 데이터 로딩 실패 - reportId: {}", reportId, e);
            return ResponseEntity
                    .status(ReportErrorCode.MOCK_DATA_LOAD_FAILED.getHttpStatus())
                    .body(ApiResponse.error(ReportErrorCode.MOCK_DATA_LOAD_FAILED));
        }
    }
//        @GetMapping("/{reportId}/behavior/real")
//    public ResponseEntity<ApiResponse<BehaviorAnalysisResponseDto>> getBehaviorAnalysis(@PathVariable String reportId) {
//        Long userId = AuthenticationUtils.getCurrentUserIdOrThrow();
//        log.info("위험운전 행동 분석 API 호출 - reportId: {}, userId: {}", reportId, userId);
//
//        BehaviorAnalysisResponseDto analysis = service.getBehaviorAnalysis(reportId);
//
//        log.info("위험운전 행동 분석 API 성공 - reportId: {}, userId: {}", reportId, userId);
//        return ResponseEntity.ok(ApiResponse.success("위험운전 행동 분석 조회가 완료되었습니다.", analysis));
//    }

    /**
     * 위험운전 행동 JSON 파일 목데이터 조회 API
     */
    @GetMapping("/{reportId}/behavior/json")
    public ResponseEntity<ApiResponse<BehaviorAnalysisResponseDto>> getBehaviorJsonMockData(@PathVariable String reportId) {
        BehaviorAnalysisResponseDto mockData = mockDataService.getBehaviorMockData(reportId);
        return ResponseEntity.ok(ApiResponse.success("위험운전 행동 JSON 목데이터 조회 완료", mockData));
    }

}
