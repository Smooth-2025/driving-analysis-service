package com.smooth.driving_analysis_service.reports.basic_summary.controller;

import com.smooth.driving_analysis_service.global.auth.AuthenticationUtils;
import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.global.exception.CommonErrorCode;
import com.smooth.driving_analysis_service.reports.basic_summary.dto.BasicSummaryResponse;
import com.smooth.driving_analysis_service.reports.basic_summary.service.BasicSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/driving-analysis/reports")
@RequiredArgsConstructor
public class BasicSummaryController {

    private final BasicSummaryService basicSummaryService;

    /**
     * 리포트 상단 요약 조회
     */
    @GetMapping("/{reportId}/basic-summary")
    public ApiResponse<BasicSummaryResponse> getBasicSummary(@PathVariable String reportId) {
        Long userId = AuthenticationUtils.getCurrentUserIdOrThrow();
        log.info("Getting basic summary for reportId={}, userId={}", reportId, userId);
        
        BasicSummaryResponse summary = basicSummaryService.getBasicSummary(reportId);
        
        if (summary == null) {
            return ApiResponse.error(CommonErrorCode.NOT_FOUND, "리포트 요약을 찾을 수 없습니다.");
        }
        
        return ApiResponse.success("리포트 상단 요약 조회 완료", summary);
    }
}