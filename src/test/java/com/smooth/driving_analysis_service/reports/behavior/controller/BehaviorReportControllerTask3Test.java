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

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BehaviorReportController.class)
@DisplayName("BehaviorReportController Task 3 테스트")
class BehaviorReportControllerTask3Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BehaviorReportService behaviorReportService;

    @Test
    @DisplayName("Task 3: GET /api/driving-analysis/reports/{reportId}/behavior - 전체 기능 포함")
    void getBehaviorAnalysis_FullFeatures_Success() throws Exception {
        // given
        String reportId = "u1_r3_20250901";
        BehaviorAnalysisResponseDto mockResponse = createFullMockResponse(reportId);

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
                
                // Task 1: totalCounts 검증
                .andExpect(jsonPath("$.data.totalCounts.hardBrake").value(38))
                .andExpect(jsonPath("$.data.totalCounts.rapidAccel").value(42))
                .andExpect(jsonPath("$.data.totalCounts.laneChange").value(17))
                .andExpect(jsonPath("$.data.totalCounts.total").value(97))
                
                // Task 2: drivingPattern 검증
                .andExpect(jsonPath("$.data.drivingPattern").exists())
                .andExpect(jsonPath("$.data.drivingPattern.weekday").value("금요일"))
                .andExpect(jsonPath("$.data.drivingPattern.timeslot").value("저녁"))
                .andExpect(jsonPath("$.data.drivingPattern.chart").isArray())
                .andExpect(jsonPath("$.data.drivingPattern.comment").exists())
                
                // Task 3: compare 검증
                .andExpect(jsonPath("$.data.compare").exists())
                .andExpect(jsonPath("$.data.compare.incdec").value(7.78))
                .andExpect(jsonPath("$.data.compare.chart.hardBrake.before").value(35))
                .andExpect(jsonPath("$.data.compare.chart.hardBrake.current").value(38))
                .andExpect(jsonPath("$.data.compare.chart.rapidAccel.before").value(40))
                .andExpect(jsonPath("$.data.compare.chart.rapidAccel.current").value(42))
                .andExpect(jsonPath("$.data.compare.chart.laneChange.before").value(15))
                .andExpect(jsonPath("$.data.compare.chart.laneChange.current").value(17));
    }

    @Test
    @DisplayName("Task 3: 첫 번째 리포트 (이전 데이터 없음)")
    void getBehaviorAnalysis_FirstReport() throws Exception {
        // given
        String reportId = "u1_r1_20250901";
        BehaviorAnalysisResponseDto mockResponse = createFirstReportMockResponse(reportId);

        when(behaviorReportService.getBehaviorAnalysis(anyString()))
                .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/driving-analysis/reports/{reportId}/behavior", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.compare.incdec").value(100.0)) // 첫 리포트는 100% 증가
                .andExpect(jsonPath("$.data.compare.chart.hardBrake.before").value(0))
                .andExpect(jsonPath("$.data.compare.chart.hardBrake.current").value(20));
    }

    @Test
    @DisplayName("Task 3: 감소 패턴")
    void getBehaviorAnalysis_DecreasePattern() throws Exception {
        // given
        String reportId = "u1_r4_20250901";
        BehaviorAnalysisResponseDto mockResponse = createDecreaseMockResponse(reportId);

        when(behaviorReportService.getBehaviorAnalysis(anyString()))
                .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/driving-analysis/reports/{reportId}/behavior", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.compare.incdec").value(-25.0)) // 25% 감소
                .andExpect(jsonPath("$.data.compare.chart.hardBrake.before").value(40))
                .andExpect(jsonPath("$.data.compare.chart.hardBrake.current").value(30));
    }

    private BehaviorAnalysisResponseDto createFullMockResponse(String reportId) {
        // 주간 차트 데이터 생성
        List<BehaviorAnalysisResponseDto.WeeklyChart> weeklyCharts = new ArrayList<>();
        String[] weekdays = {"월", "화", "수", "목", "금", "토", "일"};
        
        for (String weekday : weekdays) {
            weeklyCharts.add(BehaviorAnalysisResponseDto.WeeklyChart.builder()
                    .weekday(weekday)
                    .actions(BehaviorAnalysisResponseDto.Actions.builder()
                            .hardBrake(BehaviorAnalysisResponseDto.ActionTimeSlot.builder()
                                    .timeSlot("퇴근").count(5).build())
                            .rapidAccel(BehaviorAnalysisResponseDto.ActionTimeSlot.builder()
                                    .timeSlot("출근").count(3).build())
                            .laneChange(BehaviorAnalysisResponseDto.ActionTimeSlot.builder()
                                    .timeSlot("낮").count(2).build())
                            .build())
                    .build());
        }

        return BehaviorAnalysisResponseDto.builder()
                .reportId(reportId)
                .totalCounts(BehaviorAnalysisResponseDto.TotalCounts.builder()
                        .hardBrake(38).rapidAccel(42).laneChange(17).total(97)
                        .build())
                .drivingPattern(BehaviorAnalysisResponseDto.DrivingPattern.builder()
                        .weekday("금요일")
                        .timeslot("저녁")
                        .chart(weeklyCharts)
                        .comment("평일 저녁에는 급제동과 급가속이 늘어나는 패턴이 보여요!")
                        .build())
                .compare(BehaviorAnalysisResponseDto.Compare.builder()
                        .incdec(7.78)
                        .chart(BehaviorAnalysisResponseDto.Chart.builder()
                                .hardBrake(BehaviorAnalysisResponseDto.BeforeAfter.builder().before(35).current(38).build())
                                .rapidAccel(BehaviorAnalysisResponseDto.BeforeAfter.builder().before(40).current(42).build())
                                .laneChange(BehaviorAnalysisResponseDto.BeforeAfter.builder().before(15).current(17).build())
                                .build())
                        .build())
                .build();
    }

    private BehaviorAnalysisResponseDto createFirstReportMockResponse(String reportId) {
        return BehaviorAnalysisResponseDto.builder()
                .reportId(reportId)
                .totalCounts(BehaviorAnalysisResponseDto.TotalCounts.builder()
                        .hardBrake(20).rapidAccel(25).laneChange(10).total(55)
                        .build())
                .drivingPattern(BehaviorAnalysisResponseDto.DrivingPattern.builder()
                        .weekday("금요일")
                        .timeslot("저녁")
                        .chart(new ArrayList<>())
                        .comment("첫 번째 리포트입니다.")
                        .build())
                .compare(BehaviorAnalysisResponseDto.Compare.builder()
                        .incdec(100.0) // 첫 리포트는 100% 증가
                        .chart(BehaviorAnalysisResponseDto.Chart.builder()
                                .hardBrake(BehaviorAnalysisResponseDto.BeforeAfter.builder().before(0).current(20).build())
                                .rapidAccel(BehaviorAnalysisResponseDto.BeforeAfter.builder().before(0).current(25).build())
                                .laneChange(BehaviorAnalysisResponseDto.BeforeAfter.builder().before(0).current(10).build())
                                .build())
                        .build())
                .build();
    }

    private BehaviorAnalysisResponseDto createDecreaseMockResponse(String reportId) {
        return BehaviorAnalysisResponseDto.builder()
                .reportId(reportId)
                .totalCounts(BehaviorAnalysisResponseDto.TotalCounts.builder()
                        .hardBrake(30).rapidAccel(35).laneChange(10).total(75)
                        .build())
                .drivingPattern(BehaviorAnalysisResponseDto.DrivingPattern.builder()
                        .weekday("금요일")
                        .timeslot("저녁")
                        .chart(new ArrayList<>())
                        .comment("위험행동이 감소했습니다.")
                        .build())
                .compare(BehaviorAnalysisResponseDto.Compare.builder()
                        .incdec(-25.0) // 25% 감소
                        .chart(BehaviorAnalysisResponseDto.Chart.builder()
                                .hardBrake(BehaviorAnalysisResponseDto.BeforeAfter.builder().before(40).current(30).build())
                                .rapidAccel(BehaviorAnalysisResponseDto.BeforeAfter.builder().before(45).current(35).build())
                                .laneChange(BehaviorAnalysisResponseDto.BeforeAfter.builder().before(15).current(10).build())
                                .build())
                        .build())
                .build();
    }
}