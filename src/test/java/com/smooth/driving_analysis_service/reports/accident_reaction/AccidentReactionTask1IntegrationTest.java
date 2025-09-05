package com.smooth.driving_analysis_service.reports.accident_reaction;

import com.smooth.driving_analysis_service.reports.accident_reaction.entity.AccidentReactionMetric;
import com.smooth.driving_analysis_service.reports.accident_reaction.entity.AlertRenderEvent;
import com.smooth.driving_analysis_service.reports.accident_reaction.repository.AccidentReactionMetricRepository;
import com.smooth.driving_analysis_service.reports.accident_reaction.repository.AlertRenderEventRepository;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneItem;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneItemRepository;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class AccidentReactionTask1IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccidentReactionMetricRepository accidentReactionMetricRepository;

    @Autowired
    private AlertRenderEventRepository alertRenderEventRepository;

    @Autowired
    private MilestoneReportRepository milestoneReportRepository;

    @Autowired
    private MilestoneItemRepository milestoneItemRepository;

    private Long reportId;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 준비
        setupTestData();
    }

    private void setupTestData() {
        // 1. MilestoneReport 생성
        MilestoneReport report = MilestoneReport.builder()
                .userId(12345L)
                .cycleNo(1)
                .numberOfDriving(4)
                .status(MilestoneReport.Status.COLLECTING)
                .build();
        report = milestoneReportRepository.save(report);
        reportId = report.getId();

        // 2. MilestoneItem 생성 (4개 주행)
        String[] drivingIds = {"trip-001", "trip-002", "trip-003", "trip-004"};
        for (int i = 0; i < drivingIds.length; i++) {
            MilestoneItem item = MilestoneItem.builder()
                    .reportId(reportId)
                    .drivingId(drivingIds[i])
                    .orderNo(i + 1)
                    .build();
            milestoneItemRepository.save(item);
        }

        // 3. AlertRenderEvent 및 AccidentReactionMetric 생성 (다양한 시나리오)
        createAlertAndReaction("alert-001", 12345L, "trip-001", 1500L, true, true, false);  // 급제동 반응
        createAlertAndReaction("alert-002", 12345L, "trip-001", 2300L, true, false, true);  // 회피 반응
        createAlertAndReaction("alert-003", 12345L, "trip-002", null, false, false, false); // 반응 없음
        createAlertAndReaction("alert-004", 12345L, "trip-003", 1800L, true, true, false);  // 급제동 반응
        createAlertAndReaction("alert-005", 12345L, "trip-004", 2100L, true, false, true);  // 회피 반응
    }

    private void createAlertAndReaction(String alertId, Long userId, String drivingId, 
                                      Long responseTimeMs, Boolean responded, 
                                      Boolean decelOrStop, Boolean evasiveManeuver) {
        // AlertRenderEvent 생성
        AlertRenderEvent alertEvent = AlertRenderEvent.builder()
                .alertId(alertId)
                .userId(userId)
                .drivingId(drivingId)
                .type("accident-nearby")
                .renderedAt(LocalDateTime.now().minusMinutes(10))
                .receivedAt(LocalDateTime.now().minusMinutes(10))
                .build();
        alertRenderEventRepository.save(alertEvent);

        // AccidentReactionMetric 생성 (분석 결과)
        AccidentReactionMetric metric = AccidentReactionMetric.builder()
                .alertId(alertId)
                .userId(userId)
                .drivingId(drivingId)
                .responseTimeMs(responseTimeMs)
                .responded(responded)
                .decelOrStop(decelOrStop)
                .evasiveManeuver(evasiveManeuver)
                .windowSec(120)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        accidentReactionMetricRepository.save(metric);
    }

    @Test
    @DisplayName("Task 1 통합 테스트: 실제 데이터로 기본 반응 지표 조회")
    void getAccidentResponse_IntegrationTest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/driving-analysis/reports/{reportId}/accident-response", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.reportId").value(reportId.toString()))
                .andExpect(jsonPath("$.data.receivedAlertCount").value(5))  // 총 5건 알림
                .andExpect(jsonPath("$.data.avgReactionSec").value(1.925))  // (1.5+2.3+1.8+2.1)/4 = 1.925초
                .andExpect(jsonPath("$.data.brakeOrStopRatio").value(0.4))  // 5건 중 2건 급제동 = 0.4
                .andExpect(jsonPath("$.data.avoidRatio").value(0.4));       // 5건 중 2건 회피 = 0.4
    }

    @Test
    @DisplayName("Task 1 통합 테스트: 존재하지 않는 리포트 ID")
    void getAccidentResponse_NotExistingReportId() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/driving-analysis/reports/{reportId}/accident-response", "999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.reportId").value("999999"))
                .andExpect(jsonPath("$.data.receivedAlertCount").value(0))
                .andExpect(jsonPath("$.data.avgReactionSec").value(0.0))
                .andExpect(jsonPath("$.data.brakeOrStopRatio").value(0.0))
                .andExpect(jsonPath("$.data.avoidRatio").value(0.0));
    }
}