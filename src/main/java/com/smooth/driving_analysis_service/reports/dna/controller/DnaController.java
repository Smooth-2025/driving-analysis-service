package com.smooth.driving_analysis_service.reports.dna.controller;

import com.smooth.driving_analysis_service.global.auth.AuthenticationUtils;
import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.reports.dna.dto.response.DnaAnalysisResponseDto;
import com.smooth.driving_analysis_service.reports.dna.service.DnaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/driving-analysis/reports")
@Slf4j
public class DnaController {

    private final DnaService dnaService;

    /**
     * 운전 성향 DNA 분석 API
     */
    @GetMapping("/{reportId}/dna")
    public ResponseEntity<ApiResponse<DnaAnalysisResponseDto>> getDnaAnalysis(@PathVariable String reportId) {
        Long userId = AuthenticationUtils.getCurrentUserIdOrThrow();
        log.info("운전 성향 DNA 분석 API 호출 - reportId: {}, userId: {}", reportId, userId);
        
        DnaAnalysisResponseDto analysis = dnaService.getDnaAnalysis(reportId);

        return ResponseEntity.ok(ApiResponse.success("운전 성향 DNA 조회가 완료되었습니다.", analysis));

    }
}