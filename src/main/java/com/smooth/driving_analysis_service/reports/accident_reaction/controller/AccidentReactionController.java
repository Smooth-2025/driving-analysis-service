// reports/accident_reaction/controller/AccidentResponseController.java
package com.smooth.driving_analysis_service.reports.accident_reaction.controller;

import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.request.AccidentReactionRenderedRequestDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.ReactionComparisonResponseDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.service.AccidentReactionService;
import com.smooth.driving_analysis_service.reports.accident_reaction.service.AccidentResponseService;
import com.smooth.driving_analysis_service.reports.accident_reaction.service.ReactionComparisonService;
import com.smooth.driving_analysis_service.reports.accident_reaction.service.AccidentReactionReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/driving-analysis/reports")
public class AccidentReactionController {

    private final AccidentResponseService svc;
    private final AccidentReactionService accidentReactionService;
    private final ReactionComparisonService reactionComparisonService;
    private final AccidentReactionReportService accidentReactionReportService;

    @GetMapping("/{reportId}/accident-response")
    public ResponseEntity<?> get(@PathVariable String reportId) {
        var data = accidentReactionReportService.getFullReport(reportId);
        return ResponseEntity.ok(Map.of("success", true, "code", "SUCCESS", "message", "ok", "data", data));
    }
    
    // 사고 알림 렌더링 API
    @PostMapping("/accident-reaction/alerts/{alertId}/rendered")
    public ResponseEntity<?> recordAlertRendered(
            @PathVariable String alertId,
            @RequestParam Long userId,
            @RequestBody AccidentReactionRenderedRequestDto request) {
        
        var ack = accidentReactionService.recordAndAnalyzeAsync(
            alertId, userId, request.getRenderedAtMs(), request.getType());
        
        var response = Map.of(
            "alertId", alertId,
            "userId", userId,
            "drivingId", ack.drivingId(),
            "serverReceivedAtMs", System.currentTimeMillis(),
            "analysisScheduled", true
        );
        
        return ResponseEntity.ok(Map.of("success", true, "code", "SUCCESS", "message", "ok", "data", response));
    }
    
    // 사고 반응 요약 API
    @GetMapping("/accident-reaction/summary")
    public ResponseEntity<?> getSummary(
            @RequestParam Long userId,
            @RequestParam String from,
            @RequestParam String to) {
        
        var summary = accidentReactionService.summary(userId, from, to);
        return ResponseEntity.ok(Map.of("success", true, "code", "SUCCESS", "message", "ok", "data", summary));
    }
    
    // 반응시간 비교 API
    @GetMapping("/users/{userId}/reaction-comparison")
    public ResponseEntity<?> getReactionComparison(@PathVariable Long userId) {
        ReactionComparisonResponseDto comparison = reactionComparisonService.getReactionComparison(userId);
        return ResponseEntity.ok(Map.of("success", true, "code", "SUCCESS", "message", "ok", "data", comparison));
    }
}
