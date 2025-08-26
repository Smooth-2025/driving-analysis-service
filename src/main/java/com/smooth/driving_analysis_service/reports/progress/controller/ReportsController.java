package com.smooth.driving_analysis_service.reports.progress.controller;

import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.reports.progress.dto.ProgressResponseDto;
import com.smooth.driving_analysis_service.reports.progress.dto.ReadinessResponseDto;

import com.smooth.driving_analysis_service.reports.progress.service.ProgressService;
import com.smooth.driving_analysis_service.reports.progress.service.ReadinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/driving-analysis/reports") // 네가 쓰고 있던 prefix 유지
public class ReportsController {

    private final ProgressService progressService;
    private final ReadinessService readinessService;

    // 진행도 조회
    @GetMapping("/progress")
    public ApiResponse<ProgressResponseDto> getProgress(@RequestParam("userId") long userId) {
        var dto = progressService.getProgress(userId);
        return ApiResponse.success("OK", progressService.getProgress(userId));
    }

    // 리포트 준비 상태
    @GetMapping("/readiness")
    public ApiResponse<ReadinessResponseDto> getReadiness(@RequestParam("userId") long userId) {
        var dto = readinessService.getReadiness(userId);
        return ApiResponse.success("OK", readinessService.getReadiness(userId));
    }

}
