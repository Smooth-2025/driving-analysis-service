package com.smooth.driving_analysis_service.reports.behavior.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.BehaviorAnalysisResponseDto;
import com.smooth.driving_analysis_service.reports.behavior.service.BehaviorReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BehaviorReportController.class)
@DisplayName("BehaviorReportController Task 1 테스트")
class BehaviorReportControllerTask1Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BehaviorReportService behaviorReportService;

    @Test
    @DisplayName("Task 1: GET /api/driving-analysis/reports/{reportId}/behavior 성공")
    void getBehaviorAnalysis_Success() throws Exception {
        // given
        String reportId = "u1_r3_20250901";
        BehaviorAnalysisResponseDto mockResponse = BehaviorAnalysisResponseDto.builder()
                .reportId(reportId)
                .totalCounts(BehaviorAnalysisResponseDto.TotalCounts.builder()
                        .hardBrake(38)
                        .rapidAccel(42)
                        .laneChange(17)
                        .total(97)
                        .build())
                .build();

        when(behaviorReportService.getBehaviorAnalysis(anyString()))
                .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/driving-analysis/reports/{reportId}/behavior", reportId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.reportId").value(reportId))
                .andExpect(jsonPath("$.data.totalCounts.hardBrake").value(38))
                .andExpect(jsonPath("$.data.totalCounts.rapidAccel").value(42))
                .andExpect(jsonPath("$.data.totalCounts.laneChange").value(17))
                .andExpect(jsonPath("$.data.totalCounts.total").value(97));
    }

    @Test
    @DisplayName("Task 1: 다양한 reportId 형식 테스트")
    void getBehaviorAnalysis_DifferentReportIdFormats() throws Exception {
        // given
        String[] reportIds = {
                "u1_r1_20250901",
                "u123_r456_20250901", 
                "u1_r999_20250901"
        };

        BehaviorAnalysisResponseDto mockResponse = BehaviorAnalysisResponseDto.builder()
                .reportId("test")
                .totalCounts(BehaviorAnalysisResponseDto.TotalCounts.builder()
                        .hardBrake(0).rapidAccel(0).laneChange(0).total(0)
                        .build())
                .build();

        when(behaviorReportService.getBehaviorAnalysis(anyString()))
                .thenReturn(mockResponse);

        // when & then
        for (String reportId : reportIds) {
            mockMvc.perform(get("/api/driving-analysis/reports/{reportId}/behavior", reportId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}