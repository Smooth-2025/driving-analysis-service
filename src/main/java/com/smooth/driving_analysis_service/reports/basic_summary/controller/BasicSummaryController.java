package com.smooth.driving_analysis_service.reports.basic_summary.controller;

import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.global.exception.CommonErrorCode;
import com.smooth.driving_analysis_service.reports.basic_summary.dto.BasicSummaryResponse;
import com.smooth.driving_analysis_service.reports.basic_summary.service.BasicSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/reports/basic-summary")
@RequiredArgsConstructor
public class BasicSummaryController {

    private final BasicSummaryService basicSummaryService;

    /**
     * 리포트 상단 요약 조회
     */
    @GetMapping("/{reportId}")
    public ApiResponse<BasicSummaryResponse> getBasicSummary(@PathVariable Long reportId) {
        log.info("Getting basic summary for reportId={}", reportId);
        
        BasicSummaryResponse summary = basicSummaryService.getBasicSummary(reportId);
        
        if (summary == null) {
            return ApiResponse.error(CommonErrorCode.NOT_FOUND, "리포트 요약을 찾을 수 없습니다.");
        }
        
        return ApiResponse.success("리포트 상단 요약 조회 완료", summary);
=======
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/driving-analysis/reports")
@RequiredArgsConstructor
@Slf4j
public class BasicSummaryController {
    
    private final BasicSummaryService basicSummaryService;
    
    @GetMapping("/{reportId}/basic-summary")
    public ResponseEntity<ApiResponse<BasicSummaryResponse>> getBasicSummary(
            @PathVariable Long reportId) {
        
        log.info("기본 통계 조회 요청 - reportId: {}", reportId);
        
        try {
            BasicSummaryResponse response = basicSummaryService.getBasicSummary(reportId);
            
            return ResponseEntity.ok(ApiResponse.success(
                    "리포트 상단 요약 조회 완료",
                    response
            ));
            
        } catch (Exception e) {
            log.error("기본 통계 조회 실패 - reportId: {}", reportId, e);
            return ResponseEntity.badRequest().body(ApiResponse.<BasicSummaryResponse>builder()
                    .success(false)
                    .code(400)
                    .message("기본 통계 조회에 실패했습니다: " + e.getMessage())
                    .data(null)
                    .build());
        }
>>>>>>> origin/feat-us7.2
    }
}