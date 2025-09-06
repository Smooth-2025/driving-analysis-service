package com.smooth.driving_analysis_service.reports.accident_reaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.request.AccidentReactionRenderedRequestDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.AccidentReactionBenchmarkDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.AccidentReactionReportResponseDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.service.AccidentReactionService;
import com.smooth.driving_analysis_service.reports.accident_reaction.service.AccidentReactionReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccidentReactionController.class)
class AccidentReactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccidentReactionService accidentReactionService;

    @MockBean
    private AccidentReactionReportService accidentReactionReportService;

    @Test
    void testGetAccidentReactionReport_Success() throws Exception {
        // Given
        String reportId = "123";
        AccidentReactionBenchmarkDto.ChartDto chart = AccidentReactionBenchmarkDto.ChartDto.builder()
                .labels(new String[]{"일반 운전자", "내 주행"})
                .valuesSec(new Double[]{1.4, 1.7})
                .build();
        
        AccidentReactionBenchmarkDto benchmark = AccidentReactionBenchmarkDto.builder()
                .deltaSec(-0.3)
                .chart(chart)
                .build();
        
        AccidentReactionReportResponseDto mockResponse = AccidentReactionReportResponseDto.builder()
                .reportId(reportId)
                .receivedAlertCount(12)
                .avgReactionSec(1.7)
                .brakeOrStopRatio(0.38)
                .avoidRatio(0.12)
                .benchmark(benchmark)
                .build();

        when(accidentReactionReportService.getFullReport(reportId)).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/driving-analysis/reports/{reportId}/accident-response", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.reportId").value(reportId))
                .andExpect(jsonPath("$.data.receivedAlertCount").value(12))
                .andExpect(jsonPath("$.data.avgReactionSec").value(1.7))
                .andExpect(jsonPath("$.data.brakeOrStopRatio").value(0.38))
                .andExpect(jsonPath("$.data.avoidRatio").value(0.12))
                .andExpect(jsonPath("$.data.benchmark.deltaSec").value(-0.3))
                .andExpect(jsonPath("$.data.benchmark.chart.labels[0]").value("일반 운전자"))
                .andExpect(jsonPath("$.data.benchmark.chart.labels[1]").value("내 주행"))
                .andExpect(jsonPath("$.data.benchmark.chart.valuesSec[0]").value(1.4))
                .andExpect(jsonPath("$.data.benchmark.chart.valuesSec[1]").value(1.7));
    }

    @Test
    void testRecordAlertRendered_Success() throws Exception {
        // Given
        String alertId = "alert_123";
        Long userId = 456L;
        String drivingId = "driving_789";
        
        AccidentReactionRenderedRequestDto request = AccidentReactionRenderedRequestDto.builder()
                .renderedAtMs(1724823001230L)
                .type("accident-nearby")
                .build();

        when(accidentReactionService.recordAndAnalyzeAsync(eq(alertId), eq(userId), eq(1724823001230L), eq("accident-nearby")))
                .thenReturn(drivingId);

        // When & Then
        mockMvc.perform(post("/api/driving-analysis/reports/accident-reaction/alerts/{alertId}/rendered", alertId)
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("알림 렌더 시각 수신"))
                .andExpect(jsonPath("$.data.alertId").value(alertId))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.drivingId").value(drivingId))
                .andExpect(jsonPath("$.data.analysisScheduled").value(true))
                .andExpect(jsonPath("$.data.serverReceivedAtMs").exists());
    }

    @Test
    void testRecordAlertRendered_NullDrivingId() throws Exception {
        // Given
        String alertId = "alert_123";
        Long userId = 456L;
        
        AccidentReactionRenderedRequestDto request = AccidentReactionRenderedRequestDto.builder()
                .renderedAtMs(1724823001230L)
                .type("obstacle")
                .build();

        when(accidentReactionService.recordAndAnalyzeAsync(eq(alertId), eq(userId), eq(1724823001230L), eq("obstacle")))
                .thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/driving-analysis/reports/accident-reaction/alerts/{alertId}/rendered", alertId)
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.drivingId").value(""));
    }

    @Test
    void testGetAccidentReactionReport_EmptyData() throws Exception {
        // Given
        String reportId = "999";
        AccidentReactionBenchmarkDto.ChartDto chart = AccidentReactionBenchmarkDto.ChartDto.builder()
                .labels(new String[]{"일반 운전자", "내 주행"})
                .valuesSec(new Double[]{2.0, 0.0})
                .build();
        
        AccidentReactionBenchmarkDto benchmark = AccidentReactionBenchmarkDto.builder()
                .deltaSec(2.0)
                .chart(chart)
                .build();
        
        AccidentReactionReportResponseDto mockResponse = AccidentReactionReportResponseDto.builder()
                .reportId(reportId)
                .receivedAlertCount(0)
                .avgReactionSec(0.0)
                .brakeOrStopRatio(0.0)
                .avoidRatio(0.0)
                .benchmark(benchmark)
                .build();

        when(accidentReactionReportService.getFullReport(reportId)).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/driving-analysis/reports/{reportId}/accident-response", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.receivedAlertCount").value(0))
                .andExpect(jsonPath("$.data.avgReactionSec").value(0.0))
                .andExpect(jsonPath("$.data.benchmark.deltaSec").value(2.0));
    }
}