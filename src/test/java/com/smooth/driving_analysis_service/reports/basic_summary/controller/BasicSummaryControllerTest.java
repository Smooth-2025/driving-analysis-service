package com.smooth.driving_analysis_service.reports.basic_summary.controller;


import com.smooth.driving_analysis_service.global.auth.AuthenticationUtils;
import com.smooth.driving_analysis_service.reports.basic_summary.dto.BasicSummaryResponse;
import com.smooth.driving_analysis_service.reports.basic_summary.service.BasicSummaryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BasicSummaryController.class)
class BasicSummaryControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private BasicSummaryService basicSummaryService;
    
    private MockedStatic<AuthenticationUtils> authUtilsMock;
    
    @BeforeEach
    void setUp() {
        authUtilsMock = mockStatic(AuthenticationUtils.class);
        authUtilsMock.when(AuthenticationUtils::getCurrentUserIdOrThrow)
                     .thenReturn(1L);
    }
    
    @AfterEach
    void tearDown() {
        authUtilsMock.close();
    }
    
    @Test
    @DisplayName("기본 요약 조회 API 성공")
    void getBasicSummary_Success() throws Exception {
        // given
        Long reportId = 1L;
        BasicSummaryResponse response = BasicSummaryResponse.builder()
                .reportId("u12345_r1_20250905")
                .totalDistanceKm(26.6)
                .periodStart(LocalDate.of(2025, 8, 1))
                .periodEnd(LocalDate.of(2025, 8, 28))
                .averageDurationSec(38.25)
                .averageDistanceKm(1.77)
                .averageSpeedKmh(42.3)
                .averageCruiseRatio(0.684)
                .build();
        
        when(basicSummaryService.getBasicSummary(reportId)).thenReturn(response);
        
        // when & then
        mockMvc.perform(get("/api/driving-analysis/reports/{reportId}/basic-summary", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("리포트 상단 요약 조회 완료"))
                .andExpect(jsonPath("$.data.reportId").value("u12345_r1_20250905"))
                .andExpect(jsonPath("$.data.totalDistanceKm").value(26.6))
                .andExpect(jsonPath("$.data.periodStart").value("2025-08-01"))
                .andExpect(jsonPath("$.data.periodEnd").value("2025-08-28"))
                .andExpect(jsonPath("$.data.averageDurationSec").value(38.25))
                .andExpect(jsonPath("$.data.averageDistanceKm").value(1.77))
                .andExpect(jsonPath("$.data.averageSpeedKmh").value(42.3))
                .andExpect(jsonPath("$.data.averageCruiseRatio").value(0.684));
    }
    
    @Test
    @DisplayName("기본 요약 조회 API 실패 - 데이터 없음")
    void getBasicSummary_NotFound() throws Exception {
        // given
        Long reportId = 999L;
        
        when(basicSummaryService.getBasicSummary(reportId))
                .thenThrow(new RuntimeException("기본 통계를 찾을 수 없습니다. reportId: " + reportId));
        
        // when & then
        mockMvc.perform(get("/api/driving-analysis/reports/{reportId}/basic-summary", reportId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("기본 통계를 찾을 수 없습니다. reportId: 999"))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }
    
    @Test
    @DisplayName("잘못된 reportId 형식 테스트")
    void getBasicSummary_InvalidReportIdFormat() throws Exception {
        // when & then
        mockMvc.perform(get("/api/driving-analysis/reports/invalid/basic-summary"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("음수 reportId 테스트")
    void getBasicSummary_NegativeReportId() throws Exception {
        // given
        Long reportId = -1L;
        
        when(basicSummaryService.getBasicSummary(reportId))
                .thenThrow(new RuntimeException("잘못된 reportId입니다."));
        
        // when & then
        mockMvc.perform(get("/api/driving-analysis/reports/{reportId}/basic-summary", reportId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("잘못된 reportId입니다."));
    }
    
    @Test
    @DisplayName("예외 발생 테스트")
    void getBasicSummary_Exception() throws Exception {
        // given
        Long reportId = 1L;
        
        when(basicSummaryService.getBasicSummary(reportId))
                .thenThrow(new RuntimeException("예상치 못한 오류"));
        
        // when & then
        mockMvc.perform(get("/api/driving-analysis/reports/{reportId}/basic-summary", reportId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("예상치 못한 오류"));
    }
}