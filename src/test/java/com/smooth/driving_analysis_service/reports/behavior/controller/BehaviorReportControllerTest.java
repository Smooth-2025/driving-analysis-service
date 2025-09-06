package com.smooth.driving_analysis_service.reports.behavior.controller;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BehaviorReportController.class)
@DisplayName("BehaviorReportController 단위 테스트")
class BehaviorReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BehaviorReportService behaviorReportService;

    @Test
    @DisplayName("GET /api/reports/behavior/{reportId} - 정상 응답")
    void getBehaviorAnalysis_Success() throws Exception {
        // given
        String reportId = "u1_r3_20250901";
        BehaviorAnalysisResponseDto mockResponse = createMockResponse(reportId);
        
        when(behaviorReportService.getBehaviorAnalysis(anyString()))
                .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/driving-analysis/reports/{reportId}/behavior", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportId").value(reportId))
                .andExpect(jsonPath("$.data.totalCounts.hardBrake").value(38))
                .andExpect(jsonPath("$.data.totalCounts.rapidAccel").value(42))
                .andExpect(jsonPath("$.data.totalCounts.laneChange").value(17))
                .andExpect(jsonPath("$.data.totalCounts.total").value(97))
                .andExpect(jsonPath("$.data.drivingPattern.weekday").value("금요일"))
                .andExpect(jsonPath("$.data.drivingPattern.timeslot").value("저녁"))
                .andExpect(jsonPath("$.data.compare.incdec").value(1.04));
    }

    @Test
    @DisplayName("GET /api/reports/behavior/{reportId} - 서비스 예외 발생")
    void getBehaviorAnalysis_ServiceException() throws Exception {
        // given
        String reportId = "u1_r3_20250901";
        
        when(behaviorReportService.getBehaviorAnalysis(anyString()))
                .thenThrow(new RuntimeException("Service error"));

        // when & then - GlobalExceptionHandler가 RuntimeException을 400으로 처리
        mockMvc.perform(get("/api/driving-analysis/reports/{reportId}/behavior", reportId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/reports/behavior/{reportId} - 잘못된 reportId 형식")
    void getBehaviorAnalysis_InvalidReportId() throws Exception {
        // given
        String invalidReportId = "invalid_format";
        BehaviorAnalysisResponseDto mockResponse = createMockResponse(invalidReportId);
        
        when(behaviorReportService.getBehaviorAnalysis(anyString()))
                .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/driving-analysis/reports/{reportId}/behavior", invalidReportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportId").value(invalidReportId));
    }

    private BehaviorAnalysisResponseDto createMockResponse(String reportId) {
        return BehaviorAnalysisResponseDto.builder()
                .reportId(reportId)
                .totalCounts(BehaviorAnalysisResponseDto.TotalCounts.builder()
                        .hardBrake(38)
                        .rapidAccel(42)
                        .laneChange(17)
                        .total(97)
                        .build())
                .drivingPattern(BehaviorAnalysisResponseDto.DrivingPattern.builder()
                        .weekday("금요일")
                        .timeslot("저녁")
                        .chart(java.util.List.of())
                        .comment("평일 저녁에는 급제동과 급가속이 늘어나는 패턴이 보여요!")
                        .build())
                .compare(BehaviorAnalysisResponseDto.Compare.builder()
                        .incdec(1.04)
                        .chart(BehaviorAnalysisResponseDto.Chart.builder()
                                .hardBrake(BehaviorAnalysisResponseDto.BeforeAfter.builder()
                                        .before(35)
                                        .current(38)
                                        .build())
                                .rapidAccel(BehaviorAnalysisResponseDto.BeforeAfter.builder()
                                        .before(40)
                                        .current(42)
                                        .build())
                                .laneChange(BehaviorAnalysisResponseDto.BeforeAfter.builder()
                                        .before(15)
                                        .current(17)
                                        .build())
                                .build())
                        .build())
                .build();
    }
}