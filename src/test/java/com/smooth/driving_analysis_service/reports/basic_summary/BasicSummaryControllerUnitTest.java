package com.smooth.driving_analysis_service.reports.basic_summary;

import com.smooth.driving_analysis_service.global.auth.AuthenticationUtils;
import com.smooth.driving_analysis_service.reports.basic_summary.controller.BasicSummaryController;
import com.smooth.driving_analysis_service.reports.basic_summary.dto.BasicSummaryResponseDto;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BasicSummaryController.class)
@DisplayName("BasicSummaryController 단위테스트 - WebMvcTest")
class BasicSummaryControllerUnitTest {

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
    @DisplayName("API 호출 성공 - 200 응답")
    void getBasicSummary_Success() throws Exception {
        // given
        Long reportId = 1L;
        BasicSummaryResponseDto mockResponse = BasicSummaryResponseDto.builder()
                .reportId("u12345_r1_20250906")
                .totalDistanceKm(100.5)
                .averageSpeedKmh(60.0)
                .periodStart(LocalDate.of(2025, 8, 1))
                .periodEnd(LocalDate.of(2025, 8, 31))
                .build();

        when(basicSummaryService.getBasicSummary(reportId.toString())).thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/driving-analysis/reports/{reportId}/basic-summary", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reportId").value("u12345_r1_20250906"))
                .andExpect(jsonPath("$.data.totalDistanceKm").value(100.5))
                .andExpect(jsonPath("$.data.averageSpeedKmh").value(60.0));
    }

    @Test
    @DisplayName("API 호출 실패 - 400 에러")
    void getBasicSummary_BadRequest() throws Exception {
        // given
        Long reportId = 999L;
        
        when(basicSummaryService.getBasicSummary(reportId.toString()))
                .thenThrow(new RuntimeException("데이터를 찾을 수 없습니다"));

        // when & then
        mockMvc.perform(get("/api/driving-analysis/reports/{reportId}/basic-summary", reportId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("데이터를 찾을 수 없습니다"));
    }

    @Test
    @DisplayName("잘못된 URL 파라미터")
    void getBasicSummary_InvalidParameter() throws Exception {
        // given
        when(basicSummaryService.getBasicSummary("invalid"))
                .thenThrow(new RuntimeException("잘못된 reportId 형식입니다: invalid"));
        
        // when & then
        mockMvc.perform(get("/api/driving-analysis/reports/invalid/basic-summary"))
                .andExpect(status().isBadRequest());
    }
}