package com.smooth.driving_analysis_service.reports.dna.controller;

import com.smooth.driving_analysis_service.global.auth.AuthenticationUtils;
import com.smooth.driving_analysis_service.reports.dna.dto.response.DnaAnalysisResponseDto;
import com.smooth.driving_analysis_service.reports.dna.service.DnaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DnaController.class)
@DisplayName("DnaController 단위 테스트")
class DnaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DnaService dnaService;
    
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
    @DisplayName("DNA 분석 API 정상 호출")
    void getDnaAnalysis_Success() throws Exception {
        // given
        String reportId = "u1_r3_20250901";
        
        DnaAnalysisResponseDto mockResponse = DnaAnalysisResponseDto.builder()
                .reportId(reportId)
                .headline("적극적이며 빠른 반응형 운전자예요!")
                .radar(DnaAnalysisResponseDto.RadarDto.builder()
                        .A(68).B(55).C(72).D(60)
                        .build())
                .axes(List.of(
                        DnaAnalysisResponseDto.AxisDto.builder()
                                .id("A").label("A2").summary("일반형 출발: 무리하지 않는 적절한 가속이에요.")
                                .build(),
                        DnaAnalysisResponseDto.AxisDto.builder()
                                .id("B").label("B1").summary("사전 감속형: 미리 속도를 줄여 부드럽게 감속해요.")
                                .build(),
                        DnaAnalysisResponseDto.AxisDto.builder()
                                .id("C").label("C3").summary("공격형: 차선 변경이 잦고 추월 성향이 있어요.")
                                .build(),
                        DnaAnalysisResponseDto.AxisDto.builder()
                                .id("D").label("D2").summary("상황 유지형: 큰 무리 없이 안정적으로 대응해요.")
                                .build()
                ))
                .build();
        
        when(dnaService.getDnaAnalysis(reportId)).thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/driving-analysis/reports/{reportId}/dna", reportId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.reportId").value(reportId))
                .andExpect(jsonPath("$.data.headline").value("적극적이며 빠른 반응형 운전자예요!"))
                .andExpect(jsonPath("$.data.radar.A").value(68))
                .andExpect(jsonPath("$.data.radar.B").value(55))
                .andExpect(jsonPath("$.data.radar.C").value(72))
                .andExpect(jsonPath("$.data.radar.D").value(60))
                .andExpect(jsonPath("$.data.axes").isArray())
                .andExpect(jsonPath("$.data.axes.length()").value(4))
                .andExpect(jsonPath("$.data.axes[0].id").value("A"))
                .andExpect(jsonPath("$.data.axes[0].label").value("A2"))
                .andExpect(jsonPath("$.data.axes[0].summary").value("일반형 출발: 무리하지 않는 적절한 가속이에요."))
                .andExpect(jsonPath("$.data.axes[2].id").value("C"))
                .andExpect(jsonPath("$.data.axes[2].label").value("C3"))
                .andExpect(jsonPath("$.data.axes[2].summary").value("공격형: 차선 변경이 잦고 추월 성향이 있어요."));
    }

    @Test
    @DisplayName("DNA 분석 API - 서비스 예외 발생시 기본 응답")
    void getDnaAnalysis_ServiceException() throws Exception {
        // given
        String reportId = "u1_r3_20250901";
        
        DnaAnalysisResponseDto defaultResponse = DnaAnalysisResponseDto.builder()
                .reportId(reportId)
                .headline("무리하지 않는 차분한 주행 스타일이에요!")
                .radar(DnaAnalysisResponseDto.RadarDto.builder()
                        .A(65).B(65).C(65).D(65)
                        .build())
                .axes(List.of(
                        DnaAnalysisResponseDto.AxisDto.builder()
                                .id("A").label("A2").summary("일반형 출발: 무리하지 않는 적절한 가속이에요.")
                                .build(),
                        DnaAnalysisResponseDto.AxisDto.builder()
                                .id("B").label("B2").summary("반응 감속형: 상황에 맞춰 적절히 감속해요.")
                                .build(),
                        DnaAnalysisResponseDto.AxisDto.builder()
                                .id("C").label("C2").summary("중립형: 필요 시 적절히 차선을 변경해요.")
                                .build(),
                        DnaAnalysisResponseDto.AxisDto.builder()
                                .id("D").label("D2").summary("상황 유지형: 큰 무리 없이 안정적으로 대응해요.")
                                .build()
                ))
                .build();
        
        when(dnaService.getDnaAnalysis(reportId)).thenReturn(defaultResponse);

        // when & then
        mockMvc.perform(get("/api/driving-analysis/reports/{reportId}/dna", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reportId").value(reportId))
                .andExpect(jsonPath("$.data.headline").value("무리하지 않는 차분한 주행 스타일이에요!"))
                .andExpect(jsonPath("$.data.radar.A").value(65))
                .andExpect(jsonPath("$.data.radar.B").value(65))
                .andExpect(jsonPath("$.data.radar.C").value(65))
                .andExpect(jsonPath("$.data.radar.D").value(65));
    }

    @Test
    @DisplayName("DNA 분석 API - 다양한 reportId 형식 테스트")
    void getDnaAnalysis_VariousReportIdFormats() throws Exception {
        // given
        String[] reportIds = {
                "u1_r1_20250901",
                "u123_r456_20250901", 
                "u1_r15_20250901"
        };
        
        for (String reportId : reportIds) {
            DnaAnalysisResponseDto mockResponse = DnaAnalysisResponseDto.builder()
                    .reportId(reportId)
                    .headline("테스트 헤드라인")
                    .radar(DnaAnalysisResponseDto.RadarDto.builder().A(65).B(65).C(65).D(65).build())
                    .axes(List.of())
                    .build();
            
            when(dnaService.getDnaAnalysis(reportId)).thenReturn(mockResponse);

            // when & then
            mockMvc.perform(get("/api/driving-analysis/reports/{reportId}/dna", reportId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.reportId").value(reportId));
        }
    }
}