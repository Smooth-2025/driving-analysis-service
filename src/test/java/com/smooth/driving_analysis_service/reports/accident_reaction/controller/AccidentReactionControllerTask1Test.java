package com.smooth.driving_analysis_service.reports.accident_reaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.AccidentReactionReportResponseDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.service.AccidentReactionReportService;
import com.smooth.driving_analysis_service.reports.accident_reaction.service.AccidentReactionService;
import com.smooth.driving_analysis_service.reports.accident_reaction.service.AccidentResponseService;
import com.smooth.driving_analysis_service.reports.accident_reaction.service.ReactionComparisonService;
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

@WebMvcTest(AccidentReactionController.class)
class AccidentReactionControllerTask1Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccidentResponseService accidentResponseService;

    @MockBean
    private AccidentReactionService accidentReactionService;

    @MockBean
    private ReactionComparisonService reactionComparisonService;

    @MockBean
    private AccidentReactionReportService accidentReactionReportService;

    @Test
    @DisplayName("Task 1: GET /api/driving-analysis/reports/{reportId}/accident-response - 정상 응답")
    void getAccidentResponse_Success() throws Exception {
        // Given
        String reportId = "123";
        AccidentReactionReportResponseDto mockResponse = AccidentReactionReportResponseDto.builder()
                .reportId(reportId)
                .receivedAlertCount(12)
                .avgReactionSec(1.7)
                .brakeOrStopRatio(0.38)
                .avoidRatio(0.12)
                .build();

        when(accidentReactionReportService.getFullReport(reportId))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/driving-analysis/reports/{reportId}/accident-response", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.reportId").value("123"))
                .andExpect(jsonPath("$.data.receivedAlertCount").value(12))
                .andExpect(jsonPath("$.data.avgReactionSec").value(1.7))
                .andExpect(jsonPath("$.data.brakeOrStopRatio").value(0.38))
                .andExpect(jsonPath("$.data.avoidRatio").value(0.12));
    }

    @Test
    @DisplayName("Task 1: GET /api/driving-analysis/reports/{reportId}/accident-response - 데이터 없음")
    void getAccidentResponse_NoData() throws Exception {
        // Given
        String reportId = "999";
        AccidentReactionReportResponseDto mockResponse = AccidentReactionReportResponseDto.builder()
                .reportId(reportId)
                .receivedAlertCount(0)
                .avgReactionSec(0.0)
                .brakeOrStopRatio(0.0)
                .avoidRatio(0.0)
                .build();

        when(accidentReactionReportService.getFullReport(reportId))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/driving-analysis/reports/{reportId}/accident-response", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.reportId").value("999"))
                .andExpect(jsonPath("$.data.receivedAlertCount").value(0))
                .andExpect(jsonPath("$.data.avgReactionSec").value(0.0))
                .andExpect(jsonPath("$.data.brakeOrStopRatio").value(0.0))
                .andExpect(jsonPath("$.data.avoidRatio").value(0.0));
    }

    @Test
    @DisplayName("Task 1: GET /api/driving-analysis/reports/{reportId}/accident-response - 실제 사용 시나리오")
    void getAccidentResponse_RealScenario() throws Exception {
        // Given
        String reportId = "report_123";
        AccidentReactionReportResponseDto mockResponse = AccidentReactionReportResponseDto.builder()
                .reportId(reportId)
                .receivedAlertCount(8)
                .avgReactionSec(2.3)
                .brakeOrStopRatio(0.625) // 8건 중 5건이 급제동
                .avoidRatio(0.25)        // 8건 중 2건이 회피
                .build();

        when(accidentReactionReportService.getFullReport(reportId))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/driving-analysis/reports/{reportId}/accident-response", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.reportId").value("report_123"))
                .andExpect(jsonPath("$.data.receivedAlertCount").value(8))
                .andExpect(jsonPath("$.data.avgReactionSec").value(2.3))
                .andExpect(jsonPath("$.data.brakeOrStopRatio").value(0.625))
                .andExpect(jsonPath("$.data.avoidRatio").value(0.25));
    }
}