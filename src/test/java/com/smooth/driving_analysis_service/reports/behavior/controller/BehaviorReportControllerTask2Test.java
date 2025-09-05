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
@DisplayName("BehaviorReportController Task 2 테스트")
class BehaviorReportControllerTask2Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BehaviorReportService behaviorReportService;

    @Test
    @DisplayName("Task 2: GET /api/driving-analysis/reports/{reportId}/behavior - drivingPattern 포함")
    void getBehaviorAnalysis_WithDrivingPattern_Success() throws Exception {
        // given
        String reportId = "u1_r3_20250901";
        BehaviorAnalysisResponseDto mockResponse = createMockResponseWithDrivingPattern(reportId);

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
                
                // totalCounts 검증
                .andExpect(jsonPath("$.data.totalCounts.hardBrake").value(38))
                .andExpect(jsonPath("$.data.totalCounts.rapidAccel").value(42))
                .andExpect(jsonPath("$.data.totalCounts.laneChange").value(17))
                .andExpect(jsonPath("$.data.totalCounts.total").value(97))
                
                // drivingPattern 검증
                .andExpect(jsonPath("$.data.drivingPattern").exists())
                .andExpect(jsonPath("$.data.drivingPattern.weekday").value("금요일"))
                .andExpect(jsonPath("$.data.drivingPattern.timeslot").value("저녁"))
                .andExpect(jsonPath("$.data.drivingPattern.chart").isArray())
                .andExpect(jsonPath("$.data.drivingPattern.comment").exists())
                
                // 월요일 차트 데이터 검증
                .andExpect(jsonPath("$.data.drivingPattern.chart[0].weekday").value("월"))
                .andExpect(jsonPath("$.data.drivingPattern.chart[0].actions.hardBrake.timeSlot").value("퇴근"))
                .andExpect(jsonPath("$.data.drivingPattern.chart[0].actions.hardBrake.count").value(5));
    }

    @Test
    @DisplayName("Task 2: 패턴 데이터 없는 경우 응답 검증")
    void getBehaviorAnalysis_NoPatternData() throws Exception {
        // given
        String reportId = "u1_r999_20250901";
        BehaviorAnalysisResponseDto mockResponse = createMockResponseWithEmptyPattern(reportId);

        when(behaviorReportService.getBehaviorAnalysis(anyString()))
                .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/driving-analysis/reports/{reportId}/behavior", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.drivingPattern.comment").value(org.hamcrest.Matchers.containsString("충분한 주행 데이터가 없어")));
    }

    private BehaviorAnalysisResponseDto createMockResponseWithDrivingPattern(String reportId) {
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
                .build();
    }

    private BehaviorAnalysisResponseDto createMockResponseWithEmptyPattern(String reportId) {
        // 빈 주간 차트 데이터 생성
        List<BehaviorAnalysisResponseDto.WeeklyChart> emptyCharts = new ArrayList<>();
        String[] weekdays = {"월", "화", "수", "목", "금", "토", "일"};
        
        for (String weekday : weekdays) {
            emptyCharts.add(BehaviorAnalysisResponseDto.WeeklyChart.builder()
                    .weekday(weekday)
                    .actions(BehaviorAnalysisResponseDto.Actions.builder()
                            .hardBrake(null).rapidAccel(null).laneChange(null)
                            .build())
                    .build());
        }

        return BehaviorAnalysisResponseDto.builder()
                .reportId(reportId)
                .totalCounts(BehaviorAnalysisResponseDto.TotalCounts.builder()
                        .hardBrake(0).rapidAccel(0).laneChange(0).total(0)
                        .build())
                .drivingPattern(BehaviorAnalysisResponseDto.DrivingPattern.builder()
                        .weekday("금요일")
                        .timeslot("저녁")
                        .chart(emptyCharts)
                        .comment("아직 충분한 주행 데이터가 없어 패턴을 분석할 수 없습니다.")
                        .build())
                .build();
    }
}