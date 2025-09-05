package com.smooth.driving_analysis_service.reports.accident_reaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.AccidentReactionBenchmarkDto;
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

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccidentReactionController.class)
class AccidentReactionControllerTask2Test {

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
    @DisplayName("Task 2: GET /api/driving-analysis/reports/{reportId}/accident-response - 벤치마크 포함")
    void getAccidentResponse_WithBenchmark() throws Exception {
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
        
        // AccidentResponseService.buildAccidentResponse()를 모킹
        Map<String, Object> mockData = Map.of(
                "reportId", reportId,
                "receivedAlertCount", 12,
                "avgReactionSec", 1.7,
                "brakeOrStopRatio", 0.38,
                "avoidRatio", 0.12,
                "benchmark", Map.of(
                        "deltaSec", -0.3,
                        "chart", Map.of(
                                "labels", new String[]{"일반 운전자", "내 주행"},
                                "valuesSec", new Double[]{1.4, 1.7}
                        )
                )
        );
        
        when(accidentResponseService.buildAccidentResponse(Long.parseLong(reportId)))
                .thenReturn(mockData);

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
                .andExpect(jsonPath("$.data.avoidRatio").value(0.12))
                // Task 2 벤치마크 검증
                .andExpect(jsonPath("$.data.benchmark.deltaSec").value(-0.3))
                .andExpect(jsonPath("$.data.benchmark.chart.labels[0]").value("일반 운전자"))
                .andExpect(jsonPath("$.data.benchmark.chart.labels[1]").value("내 주행"))
                .andExpect(jsonPath("$.data.benchmark.chart.valuesSec[0]").value(1.4))
                .andExpect(jsonPath("$.data.benchmark.chart.valuesSec[1]").value(1.7));
    }

    @Test
    @DisplayName("Task 2: GET /api/driving-analysis/reports/{reportId}/accident-response - 내가 더 빠른 경우")
    void getAccidentResponse_FasterThanAverage() throws Exception {
        // Given
        String reportId = "456";
        
        // AccidentResponseService.buildAccidentResponse()를 모킹
        Map<String, Object> mockData = Map.of(
                "reportId", reportId,
                "receivedAlertCount", 8,
                "avgReactionSec", 1.3,
                "brakeOrStopRatio", 0.5,
                "avoidRatio", 0.25,
                "benchmark", Map.of(
                        "deltaSec", 0.8,  // 더 빠름
                        "chart", Map.of(
                                "labels", new String[]{"일반 운전자", "내 주행"},
                                "valuesSec", new Double[]{2.1, 1.3}
                        )
                )
        );
        
        when(accidentResponseService.buildAccidentResponse(Long.parseLong(reportId)))
                .thenReturn(mockData);

        // When & Then
        mockMvc.perform(get("/api/driving-analysis/reports/{reportId}/accident-response", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.benchmark.deltaSec").value(0.8))  // 양수 = 더 빠름
                .andExpect(jsonPath("$.data.benchmark.chart.valuesSec[0]").value(2.1))  // 일반 평균
                .andExpect(jsonPath("$.data.benchmark.chart.valuesSec[1]").value(1.3)); // 내 평균
    }
}