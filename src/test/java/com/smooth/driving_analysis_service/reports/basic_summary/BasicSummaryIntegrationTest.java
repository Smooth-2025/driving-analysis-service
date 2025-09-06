package com.smooth.driving_analysis_service.reports.basic_summary;

import com.smooth.driving_analysis_service.reports.basic_summary.dto.BasicSummaryResponse;
import com.smooth.driving_analysis_service.reports.basic_summary.entity.BasicSummary;
import com.smooth.driving_analysis_service.reports.basic_summary.repository.BasicSummaryRepository;
import com.smooth.driving_analysis_service.pipeline.repository.DrivingAccumulatedStatsRepository;
import com.smooth.driving_analysis_service.reports.basic_summary.service.BasicSummaryService;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneItemRepository;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BasicSummaryIntegrationTest {
    
    @Autowired
    private BasicSummaryService basicSummaryService;
    
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
        // drivingAccumulatedStatsRepository.deleteAll(); // 실제 데이터가 없으므로 주석 처리
    }
    
    @Test
    @DisplayName("FINAL 스냅샷 조회 테스트")
    void getBasicSummary_FinalSnapshot() {
        // given
        MilestoneReport report = createMilestoneReport();
        BasicSummary finalSnapshot = createBasicSummary(report.getId(), BasicSummary.SnapshotType.FINAL);
        
        // when
        BasicSummaryResponse response = basicSummaryService.getBasicSummary(report.getId());
        
        // then
        assertThat(response.getReportId()).contains("u" + TEST_USER_ID + "_r" + report.getId());
        assertThat(response.getTotalDistanceKm()).isEqualTo(26.6);
        assertThat(response.getAverageDurationSec()).isEqualTo(38.25);
        assertThat(response.getAverageDistanceKm()).isEqualTo(1.77);
        assertThat(response.getAverageSpeedKmh()).isEqualTo(42.3);
        assertThat(response.getAverageCruiseRatio()).isEqualTo(0.684);
        assertThat(response.getPeriodStart()).isEqualTo(LocalDate.of(2025, 8, 1));
        assertThat(response.getPeriodEnd()).isEqualTo(LocalDate.of(2025, 8, 28));
    }
    
    @Test
    @DisplayName("INTERIM 스냅샷 조회 테스트 (FINAL이 없는 경우)")
    void getBasicSummary_InterimSnapshot() {
        // given
        MilestoneReport report = createMilestoneReport();
        BasicSummary interimSnapshot = createBasicSummary(report.getId(), BasicSummary.SnapshotType.INTERIM);
        
        // when
        BasicSummaryResponse response = basicSummaryService.getBasicSummary(report.getId());
        
        // then
        assertThat(response.getTotalDistanceKm()).isEqualTo(26.6);
        assertThat(response.getAverageDurationSec()).isEqualTo(38.25);
    }
    
    @Test
    @DisplayName("FINAL 스냅샷이 INTERIM보다 우선 조회됨")
    void getBasicSummary_FinalTakesPrecedence() {
        // given
        MilestoneReport report = createMilestoneReport();
        
        // INTERIM 스냅샷 생성
        BasicSummary interimSnapshot = BasicSummary.builder()
                .reportId(report.getId())
                .userId(TEST_USER_ID)
                .totalDistanceKm(BigDecimal.valueOf(15.0)) // 다른 값
                .periodStart(LocalDate.of(2025, 8, 1))
                .periodEnd(LocalDate.of(2025, 8, 15))
                .averageDurationSec(BigDecimal.valueOf(30.0))
                .averageDistanceKm(BigDecimal.valueOf(1.5))
                .averageSpeedKmh(BigDecimal.valueOf(40.0))
                .averageCruiseRatio(BigDecimal.valueOf(0.65))
                .snapshotType(BasicSummary.SnapshotType.INTERIM)
                .build();
        basicSummaryRepository.save(interimSnapshot);
        
        // FINAL 스냅샷 생성
        BasicSummary finalSnapshot = createBasicSummary(report.getId(), BasicSummary.SnapshotType.FINAL);
        
        // when
        BasicSummaryResponse response = basicSummaryService.getBasicSummary(report.getId());
        
        // then - FINAL 스냅샷 값이 반환되어야 함
        assertThat(response.getTotalDistanceKm()).isEqualTo(26.6); // FINAL 값
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
    
    private BasicSummary createBasicSummary(Long reportId, BasicSummary.SnapshotType snapshotType) {
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
                .snapshotType(snapshotType)
                .build();
        return basicSummaryRepository.save(summary);
    }
}