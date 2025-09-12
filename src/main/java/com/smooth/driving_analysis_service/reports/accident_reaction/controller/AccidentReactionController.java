package com.smooth.driving_analysis_service.reports.accident_reaction.controller;

import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.global.auth.AuthenticationUtils;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.request.AlertRenderRequestDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.AccidentReactionReportResponseDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.service.AccidentReactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/driving-analysis/reports")
@RequiredArgsConstructor
public class AccidentReactionController {

    private final AccidentReactionService accidentReactionService;

    /**
     * 사고 반응 리포트 조회
     */
    @GetMapping("/{reportId}/accident-response")
    public ResponseEntity<ApiResponse<AccidentReactionReportResponseDto>> getAccidentReactionReport(
            @PathVariable String reportId) {

        Long userId = AuthenticationUtils.getCurrentUserIdOrThrow();
        log.info("사고 반응 분석 API 호출 reportId={}, userId={}", reportId, userId);

        AccidentReactionReportResponseDto report = accidentReactionService.getAccidentReactionReport(reportId);

        return ResponseEntity.ok(ApiResponse.success("사고 반응 분석 조회 완료", report));
    }

    /**
     * 알림 렌더링 이벤트 처리
     */
    @PostMapping("/accident-reaction/alerts/{alertId}/rendered")
    public ResponseEntity<ApiResponse<Void>> processAlertRender(
            @PathVariable String alertId,
            @RequestBody AlertRenderRequestDto request) {

        Long userId = AuthenticationUtils.getCurrentUserIdOrThrow();
        log.info("알림 렌더링 이벤트 처리 alertId={}, userId={}, renderedAtMs={}",
                alertId, userId, request.getRenderedAtMs());

        accidentReactionService.processAlertRender(alertId, request);

        return ResponseEntity.ok(ApiResponse.success("알림 렌더링 이벤트 처리 완료", null));
    }
}