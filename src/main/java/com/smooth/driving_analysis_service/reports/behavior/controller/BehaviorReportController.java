package com.smooth.driving_analysis_service.reports.behavior.controller;

import com.smooth.driving_analysis_service.reports.behavior.dto.request.BehaviorDiffRequestDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.*;
import com.smooth.driving_analysis_service.reports.behavior.service.BehaviorReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/driving-analysis/reports")
public class BehaviorReportController {

    private final BehaviorReportService service;

    /** 새로운 통합 위험운전 행동 분석 API */
    @GetMapping("/{reportId}/behavior")
    public ResponseEntity<?> getBehaviorAnalysis(@PathVariable String reportId) {
        BehaviorAnalysisResponseDto analysis = service.getBehaviorAnalysis(reportId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "code", "SUCCESS",
                "message", "ok",
                "data", analysis
        ));
    }

    // === 기존 API들 (하위 호환성) ===

    /** 0) 총합 요약 */
    @GetMapping("/behavior/{reportId}/summary")
    public ResponseEntity<BehaviorSummaryResponseDto> summary(@PathVariable Long reportId) {
        return ResponseEntity.ok(service.getSummary(reportId));
    }

    /** A) 시간대 궤적 */
    @GetMapping("/behavior/{reportId}/trajectory")
    public ResponseEntity<BehaviorTrajectoryResponseDto> trajectory(@PathVariable Long reportId) {
        return ResponseEntity.ok(service.getTrajectory(reportId));
    }

    /** B) 증감 요약 (prevReportId는 @RequestParam 또는 RequestBody로 전달 가능) */
    @GetMapping("/behavior/{reportId}/diff")
    public ResponseEntity<List<BehaviorDiffResponseDto>> diff(
            @PathVariable Long reportId,
            @RequestParam(required = false) Long prevReportId
    ) {
        BehaviorDiffRequestDto req = new BehaviorDiffRequestDto();
        req.setPrevReportId(prevReportId);
        return ResponseEntity.ok(service.getDiff(reportId, req));
    }

    /** C) 자동 코멘트 */
    @GetMapping("/behavior/{reportId}/comment")
    public ResponseEntity<BehaviorCommentResponseDto> comment(
            @PathVariable Long reportId,
            @RequestParam(required = false) Long prevReportId
    ) {
        BehaviorDiffRequestDto req = new BehaviorDiffRequestDto();
        req.setPrevReportId(prevReportId);
        return ResponseEntity.ok(service.getComment(reportId, req));
    }
}
