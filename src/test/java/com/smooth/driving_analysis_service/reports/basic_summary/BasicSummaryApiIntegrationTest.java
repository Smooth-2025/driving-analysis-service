package com.smooth.driving_analysis_service.reports.basic_summary;

import com.smooth.driving_analysis_service.reports.basic_summary.entity.BasicSummary;
import com.smooth.driving_analysis_service.pipeline.entity.DrivingAccumulatedStats;
import com.smooth.driving_analysis_service.reports.basic_summary.repository.BasicSummaryRepository;
import com.smooth.driving_analysis_service.pipeline.repository.DrivingAccumulatedStatsRepository;
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

import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class BasicSummaryApiIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private BasicSummaryRepository basicSummaryRepository;
    
    @Autowired
    private MilestoneReportRepository milestoneReportRepository;
    
    @Autowired
    private MilestoneItemRepository milestoneItemRepository;
    
    @Autowired
    private DrivingAccumulatedStatsRepository drivingAccumulatedStatsRepository;
    
    private static final Long TEST_USER_ID = 12345L;
    
    @BeforeEach
    void setUp() {
        // 테스트 데이터 정리
        basicSummaryRepository.deleteAll();
        milestoneItemRepository.deleteAll();
        milestoneReportRepository.deleteAll();
        drivingAccumulatedStatsRepository.deleteAll();
    }
    
    @Test
    @DisplayName("실제 데이터로 기본 요약 API 테스트")
    void getBasicSummary_WithRealData() throws Exception {
        // given
        MilestoneReport report = createMilestoneReport();
        createTestDrivingData(report.getId());
        createBasicSummarySnapshot(report.getId());
        
        // when & then
        mockMvc.perform(get("/api/driving-analysis/reports/{reportId}/basic-summary", report.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("리포트 상단 요약 조회 완료"))
                .andExpect(jsonPath("$.data.reportId").exists())
                .andExpect(jsonPath("$.data.totalDistanceKm").value(26.6))
                .andExpect(jsonPath("$.data.periodStart").value("2025-08-01"))
                .andExpect(jsonPath("$.data.periodEnd").value("2025-08-28"))
                .andExpect(jsonPath("$.data.averageDurationSec").value(38.25))
                .andExpect(jsonPath("$.data.averageDistanceKm").value(1.77))
                .andExpect(jsonPath("$.data.averageSpeedKmh").value(42.3))
                .andExpect(jsonPath("$.data.averageCruiseRatio").value(0.684));
    }
    
    @Test
    @DisplayName("존재하지 않는 reportId로 API 호출 테스트")
    void getBasicSummary_NotFound() throws Exception {
        // given
        Long nonExistentReportId = 999L;
        
        // when & then
        mockMvc.perform(get("/api/driving-analysis/reports/{reportId}/basic-summary", nonExistentReportId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("기본 통계를 찾을 수 없습니다. reportId: 999"));
    }
    
    private MilestoneReport createMilestoneReport() {
        MilestoneReport report = MilestoneReport.builder()
                .userId(TEST_USER_ID)
                .cycleNo(1)
                .numberOfDriving(15)
                .status(MilestoneReport.Status.COMPLETED)
                .reportId("test-report-id")
                .build();
        return milestoneReportRepository.save(report);
    }
    
    private void createTestDrivingData(Long reportId) {
        // MilestoneReport 조회
        MilestoneReport report = milestoneReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        
        // 15개의 주행 데이터 생성
        for (int i = 1; i <= 15; i++) {
            // DrivingAccumulatedStats 생성
            DrivingAccumulatedStats stats = DrivingAccumulatedStats.builder()
                    .userId(TEST_USER_ID)
                    .drivingId("trip-" + String.format("%03d", i))
                    .drivingMinutes(30 + i)
                    .totalDistance(15000 + (i * 1000))
                    .laneChangeCount(3 + (i % 3))
                    .hardBrakeCount(i % 2)
                    .rapidAccelCount(1 + (i % 3))
                    .avgSpeed(40.0 + i)
                    .cruiseRatio(0.6 + (i * 0.01))
                    .startTime(LocalDateTime.of(2025, 8, i, 10, 0))
                    .endTime(LocalDateTime.of(2025, 8, i, 10, 30 + i))
                    .build();
            drivingAccumulatedStatsRepository.save(stats);
            
            // MilestoneItem 생성
            MilestoneItem item = MilestoneItem.builder()
                    .reportId(report.getId())
                    .drivingId("trip-" + String.format("%03d", i))
                    .orderNo(i)
                    .build();
            milestoneItemRepository.save(item);
        }
    }
    
    private void createBasicSummarySnapshot(Long reportId) {
        BasicSummary summary = BasicSummary.builder()
                .reportId(reportId)
                .userId(TEST_USER_ID)
                .totalDistanceKm(BigDecimal.valueOf(26.6))
                .periodStart(LocalDate.of(2025, 8, 1))
                .periodEnd(LocalDate.of(2025, 8, 28))
                .averageDurationSec(BigDecimal.valueOf(38.25))
                .averageDistanceKm(BigDecimal.valueOf(1.77))
                .averageSpeedKmh(BigDecimal.valueOf(42.3))
                .averageCruiseRatio(BigDecimal.valueOf(0.684))
                .snapshotType(BasicSummary.SnapshotType.FINAL)
                .build();
        basicSummaryRepository.save(summary);
    }
}