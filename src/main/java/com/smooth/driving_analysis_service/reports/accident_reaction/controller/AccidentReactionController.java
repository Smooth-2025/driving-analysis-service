package com.smooth.driving_analysis_service.reports.accident_reaction.controller;

import com.smooth.driving_analysis_service.reports.accident_reaction.dto.request.AccidentReactionRenderedRequestDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.AccidentReactionReportResponseDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.service.AccidentReactionService;
import com.smooth.driving_analysis_service.reports.accident_reaction.service.AccidentReactionReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/driving-analysis/reports")
public class AccidentReactionController {

    private final AccidentReactionService accidentReactionService;
    private final AccidentReactionReportService accidentReactionReportService;

    /**
     * 사고 반응 리포트 조회 (Task 1 + Task 2)
     */
    @GetMapping("/{reportId}/accident-response")
    public ResponseEntity<?> getAccidentReactionReport(@PathVariable String reportId) {
        AccidentReactionReportResponseDto data = accidentReactionReportService.getFullReport(reportId);
        return ResponseEntity.ok(Map.of("success", true, "code", "SUCCESS", "message", "ok", "data", data));
    }
    
    /**
     * 사고 알림 렌더링 이벤트 수신 API
     */
    @PostMapping("/accident-reaction/alerts/{alertId}/rendered")
    public ResponseEntity<?> recordAlertRendered(
            @PathVariable String alertId,
            @RequestParam Long userId,
            @RequestBody AccidentReactionRenderedRequestDto request) {
        
        String drivingId = accidentReactionService.recordAndAnalyzeAsync(
            alertId, userId, request.getRenderedAtMs(), request.getType());
        
        Map<String, Object> response = Map.of(
            "alertId", alertId,
            "userId", userId,
            "drivingId", drivingId != null ? drivingId : "",
            "serverReceivedAtMs", System.currentTimeMillis(),
            "analysisScheduled", true
        );
        
        return ResponseEntity.ok(Map.of("success", true, "code", "SUCCESS", "message", "알림 렌더 시각 수신", "data", response));
    }
}
