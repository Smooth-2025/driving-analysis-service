package com.smooth.driving_analysis_service.reports.behavior.controller;

import com.smooth.driving_analysis_service.reports.behavior.dto.request.BehaviorDiffRequestDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.*;
import com.smooth.driving_analysis_service.reports.behavior.service.BehaviorReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/driving-analysis/reports/behavior")
public class BehaviorReportController {

    private final BehaviorReportService service;

    /** 0) 총합 요약 */
    @GetMapping("/{reportId}/summary")
    public ResponseEntity<BehaviorSummaryResponseDto> summary(@PathVariable Long reportId) {
        return ResponseEntity.ok(service.getSummary(reportId));
    }

    /** A) 시간대 궤적 */
    @GetMapping("/{reportId}/trajectory")
    public ResponseEntity<BehaviorTrajectoryResponseDto> trajectory(@PathVariable Long reportId) {
        return ResponseEntity.ok(service.getTrajectory(reportId));
    }

    /** B) 증감 요약 (prevReportId는 @RequestParam 또는 RequestBody로 전달 가능) */
    @GetMapping("/{reportId}/diff")
    public ResponseEntity<List<BehaviorDiffResponseDto>> diff(
            @PathVariable Long reportId,
            @RequestParam(required = false) Long prevReportId
    ) {
        BehaviorDiffRequestDto req = new BehaviorDiffRequestDto();
        req.setPrevReportId(prevReportId);
        return ResponseEntity.ok(service.getDiff(reportId, req));
    }

    /** C) 자동 코멘트 */
    @GetMapping("/{reportId}/comment")
    public ResponseEntity<BehaviorCommentResponseDto> comment(
            @PathVariable Long reportId,
            @RequestParam(required = false) Long prevReportId
    ) {
        BehaviorDiffRequestDto req = new BehaviorDiffRequestDto();
        req.setPrevReportId(prevReportId);
        return ResponseEntity.ok(service.getComment(reportId, req));
    }
}
