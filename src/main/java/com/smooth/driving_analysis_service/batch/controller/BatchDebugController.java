package com.smooth.driving_analysis_service.batch.controller;

import com.smooth.driving_analysis_service.batch.service.BatchReportService;
import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.global.exception.CommonErrorCode;
import com.smooth.driving_analysis_service.reports.milestone.service.MilestoneService;
import com.smooth.driving_analysis_service.trigger.dto.ReportTriggerV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/batch/debug")
@RequiredArgsConstructor
public class BatchDebugController {

    private final BatchReportService batchReportService;
    private final MilestoneService milestoneService;

    /**
     * 수동으로 리포트 트리거 처리 (테스트용)
     */
    @PostMapping("/trigger-report")
    public ApiResponse<String> triggerReport(
            @RequestParam String type,
            @RequestParam Long reportId,
            @RequestParam Long userId,
            @RequestParam int milestone,
            @RequestParam(required = false) List<String> drivingIds) {
        
        log.info("Manual report trigger: type={}, reportId={}, milestone={}", type, reportId, milestone);
        
        ReportTriggerV1 trigger = ReportTriggerV1.builder()
                .v(1)
                .type(type)
                .userId(String.valueOf(userId))
                .reportId(reportId)
                .milestone(milestone)
                .drivingIds(drivingIds != null ? drivingIds : List.of())
                .status("PROCESSING")
                .emittedAt(LocalDateTime.now())
                .producer("debug-controller")
                .traceId(UUID.randomUUID().toString())
                .build();
        
        try {
            batchReportService.processReportTrigger(trigger);
            return ApiResponse.success("리포트 트리거 처리 완료", "OK");
        } catch (Exception e) {
            log.error("Failed to process manual trigger", e);
            return ApiResponse.error(CommonErrorCode.INTERNAL_SERVER_ERROR, "리포트 트리거 처리 실패: " + e.getMessage());
        }
    }

    /**
     * 리포트 완료 처리 (테스트용)
     */
    @PostMapping("/complete-report/{reportId}")
    public ApiResponse<String> completeReport(@PathVariable Long reportId) {
        log.info("Manual report completion: reportId={}", reportId);
        
        try {
            milestoneService.markReportCompleted(reportId);
            return ApiResponse.success("리포트 완료 처리됨", "OK");
        } catch (Exception e) {
            log.error("Failed to complete report", e);
            return ApiResponse.error(CommonErrorCode.INTERNAL_SERVER_ERROR, "리포트 완료 처리 실패: " + e.getMessage());
        }
    }

    /**
     * 배치 처리 수동 실행 (테스트용)
     */
    @PostMapping("/process-pending")
    public ApiResponse<String> processPending() {
        log.info("Manual batch processing triggered");
        
        try {
            batchReportService.processPendingReports();
            return ApiResponse.success("배치 처리 완료", "OK");
        } catch (Exception e) {
            log.error("Failed to process pending reports", e);
            return ApiResponse.error(CommonErrorCode.INTERNAL_SERVER_ERROR, "배치 처리 실패: " + e.getMessage());
        }
    }
}