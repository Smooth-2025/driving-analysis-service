package com.smooth.driving_analysis_service.reports.accident_reaction.controller;

import com.smooth.driving_analysis_service.global.auth.AuthenticationUtils;
import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.request.AccidentReactionRenderedRequestDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.AccidentReactionReportResponseDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.AccidentReactionBenchmarkDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.service.AccidentReactionService;
import com.smooth.driving_analysis_service.reports.accident_reaction.service.AccidentReactionReportService;
import com.smooth.driving_analysis_service.reports.common.service.MockDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/driving-analysis/reports")
public class AccidentReactionController {

        private final AccidentReactionService accidentReactionService;
        private final AccidentReactionReportService accidentReactionReportService;
        private final MockDataService mockDataService;

        /**
         * 사고 반응 리포트 조회 (Task 1 + Task 2)
         */
        @GetMapping("/{reportId}/accident-response")
        public ApiResponse<AccidentReactionReportResponseDto> getAccidentReactionReport(@PathVariable String reportId) {
                Long userId = AuthenticationUtils.getCurrentUserIdOrThrow();
                AccidentReactionReportResponseDto data = accidentReactionReportService.getFullReport(reportId);
                return ApiResponse.success("사고 반응 리포트를 조회했습니다.", data);
        }

        /**
         * 사고 알림 렌더링 이벤트 수신 API (기존)
         */
        @PostMapping("/accident-reaction/alerts/{alertId}/rendered")
        public ResponseEntity<ApiResponse<Map<String, Object>>> recordAlertRendered(
                        @PathVariable String alertId,
                        @RequestBody AccidentReactionRenderedRequestDto request) {
                log.info("사고 알림 렌더링 이벤트 수신 - alertId: {}, renderedAtMs: {}, type: {}",
                                alertId, request.getRenderedAtMs(), request.getType());

                try {
                        Long userId = AuthenticationUtils.getCurrentUserIdOrThrow();
                        log.info("인증된 사용자 ID: {}", userId);

                        String drivingId = accidentReactionService.recordAndAnalyzeAsync(
                                        alertId, userId, request.getRenderedAtMs(), request.getType());

                        Map<String, Object> responseData = Map.of(
                                        "alertId", alertId,
                                        "userId", userId,
                                        "drivingId", drivingId != null ? drivingId : "",
                                        "serverReceivedAtMs", System.currentTimeMillis(),
                                        "analysisScheduled", true);

                        log.info("사고 알림 렌더링 이벤트 처리 완료 - drivingId: {}", drivingId);

                        ApiResponse<Map<String, Object>> response = ApiResponse.success("알림 렌더 시각 수신", responseData);

                        return ResponseEntity
                                        .ok()
                                        .header("X-Alert-Id", alertId)
                                        .header("X-User-Id", String.valueOf(userId))
                                        .header("X-Driving-Id", drivingId != null ? drivingId : "")
                                        .header("X-Server-Timestamp", String.valueOf(System.currentTimeMillis()))
                                        .header("Content-Type", "application/json")
                                        .body(response);
                } catch (Exception e) {
                        log.error("사고 알림 렌더링 이벤트 처리 중 오류 발생", e);
                        throw e;
                }
        }

        /**
         * 사고 알림 렌더링 이벤트 수신 API (프론트엔드용 - alertId 자동 생성)
         */
        @PostMapping("/accident-reaction/alerts/rendered")
        public ResponseEntity<ApiResponse<Map<String, Object>>> recordAlertRenderedWithAutoId(
                        @RequestBody AccidentReactionRenderedRequestDto request) {

                // alertId 자동 생성 (alrt_ 접두사 + 타임스탬프 + 랜덤)
                String alertId = "alrt_" + System.currentTimeMillis() + "_" +
                                java.util.UUID.randomUUID().toString().substring(0, 8);

                log.info("사고 알림 렌더링 이벤트 수신 (자동 ID) - alertId: {}, renderedAtMs: {}, type: {}",
                                alertId, request.getRenderedAtMs(), request.getType());

                try {
                        Long userId = AuthenticationUtils.getCurrentUserIdOrThrow();
                        log.info("인증된 사용자 ID: {}", userId);

                        String drivingId = accidentReactionService.recordAndAnalyzeAsync(
                                        alertId, userId, request.getRenderedAtMs(), request.getType());

                        Map<String, Object> responseData = Map.of(
                                        "alertId", alertId,
                                        "userId", userId,
                                        "drivingId", drivingId != null ? drivingId : "",
                                        "serverReceivedAtMs", System.currentTimeMillis(),
                                        "analysisScheduled", true);

                        log.info("사고 알림 렌더링 이벤트 처리 완료 - alertId: {}, drivingId: {}", alertId, drivingId);

                        ApiResponse<Map<String, Object>> response = ApiResponse.success("알림 렌더 시각 수신", responseData);

                        return ResponseEntity
                                        .ok()
                                        .header("X-Alert-Id", alertId)
                                        .header("X-User-Id", String.valueOf(userId))
                                        .header("X-Driving-Id", drivingId != null ? drivingId : "")
                                        .header("X-Server-Timestamp", String.valueOf(System.currentTimeMillis()))
                                        .header("Content-Type", "application/json")
                                        .body(response);
                } catch (Exception e) {
                        log.error("사고 알림 렌더링 이벤트 처리 중 오류 발생", e);

                        ApiResponse<Map<String, Object>> errorResponse = ApiResponse.<Map<String, Object>>builder()
                                        .success(false)
                                        .code(500)
                                        .message("서버 오류: " + e.getMessage())
                                        .data(null)
                                        .build();

                        return ResponseEntity
                                        .status(500)
                                        .header("Content-Type", "application/json")
                                        .body(errorResponse);
                }
        }

        /**
         * 사고 반응 목데이터 조회 API
         */
        @GetMapping("/{reportId}/accident-response/mock")
        public ApiResponse<AccidentReactionReportResponseDto> getAccidentReactionMockData(
                        @PathVariable String reportId) {
                AccidentReactionReportResponseDto mockData = AccidentReactionReportResponseDto.builder()
                                .reportId(reportId)
                                .receivedAlertCount(15)
                                .avgReactionSec(2.3)
                                .brakeOrStopRatio(0.67)
                                .avoidRatio(0.33)
                                .benchmark(AccidentReactionBenchmarkDto.builder()
                                                .deltaSec(0.4) // 일반 평균보다 0.4초 빠름
                                                .chart(AccidentReactionBenchmarkDto.ChartDto.builder()
                                                                .labels(new String[] { "일반 운전자", "내 주행" })
                                                                .valuesSec(new Double[] { 2.7, 2.3 })
                                                                .build())
                                                .build())
                                .build();

                return ApiResponse.success("사고 반응 목데이터 조회 완료", mockData);
        }

        /**
         * 사고 반응 JSON 파일 목데이터 조회 API
         */
        @GetMapping("/{reportId}/accident-response/json")
        public ApiResponse<AccidentReactionReportResponseDto> getAccidentReactionJsonMockData(@PathVariable String reportId) {
                AccidentReactionReportResponseDto mockData = mockDataService.getAccidentReactionMockData(reportId);
                return ApiResponse.success("사고 반응 JSON 목데이터 조회 완료", mockData);
        }
}
