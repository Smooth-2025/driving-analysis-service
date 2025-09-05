package com.smooth.driving_analysis_service.reports.behavior;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Behavior Task 1+2 통합 테스트")
class BehaviorTask1IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Task 1+2: API 엔드포인트 통합 테스트 - totalCounts + drivingPattern")
    void getBehaviorAnalysis_Integration() throws Exception {
        // given
        String reportId = "u1_r1_20250901";

        // when & then
        mockMvc.perform(get("/api/driving-analysis/reports/{reportId}/behavior", reportId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.reportId").value(reportId))
                
                // Task 1: totalCounts 검증
                .andExpect(jsonPath("$.data.totalCounts").exists())
                .andExpect(jsonPath("$.data.totalCounts.hardBrake").exists())
                .andExpect(jsonPath("$.data.totalCounts.rapidAccel").exists())
                .andExpect(jsonPath("$.data.totalCounts.laneChange").exists())
                .andExpect(jsonPath("$.data.totalCounts.total").exists())
                
                // Task 2: drivingPattern 검증
                .andExpect(jsonPath("$.data.drivingPattern").exists())
                .andExpect(jsonPath("$.data.drivingPattern.weekday").exists())
                .andExpect(jsonPath("$.data.drivingPattern.timeslot").exists())
                .andExpect(jsonPath("$.data.drivingPattern.chart").exists())
                .andExpect(jsonPath("$.data.drivingPattern.comment").exists());
    }
}