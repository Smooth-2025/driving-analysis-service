package com.smooth.driving_analysis_service.reports.basic_summary.service;

import com.smooth.driving_analysis_service.pipeline.repository.DrivingAccumulatedStatsRepository;
import com.smooth.driving_analysis_service.reports.basic_summary.dto.BasicSummaryResponse;
import com.smooth.driving_analysis_service.reports.basic_summary.entity.BasicSummary;
import com.smooth.driving_analysis_service.reports.basic_summary.repository.BasicSummaryRepository;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BasicSummaryServiceImpl 단위테스트 - Mock 기반")
class BasicSummaryServiceImplTest {

    @Mock
    private BasicSummaryRepository basicSummaryRepository;

    @Mock
    private DrivingAccumulatedStatsRepository drivingAccumulatedStatsRepository;

    @Mock
    private MilestoneReportRepository milestoneReportRepository;

    @InjectMocks
    private BasicSummaryServiceImpl basicSummaryService;

    private Long testReportId = 1L;
    private Long testUserId = 12345L;
    private BasicSummary testBasicSummary;
    private MilestoneReport testMilestoneReport;

    @BeforeEach
    void setUp() {
        testBasicSummary = BasicSummary.builder()
                .id(1L)
                .reportId(testReportId)
                .userId(testUserId)
                .snapshotType(BasicSummary.SnapshotType.FINAL)
                .totalDistanceKm(BigDecimal.valueOf(26.6))
                .averageDurationSec(BigDecimal.valueOf(38.25))
                .averageDistanceKm(BigDecimal.valueOf(1.77))
                .averageSpeedKmh(BigDecimal.valueOf(42.3))
                .averageCruiseRatio(BigDecimal.valueOf(0.684))
                .periodStart(LocalDate.of(2025, 8, 1))
                .periodEnd(LocalDate.of(2025, 8, 28))
                .build();

        testMilestoneReport = MilestoneReport.builder()
                .id(testReportId)
                .userId(testUserId)
                .cycleNo(1)
                .numberOfDriving(15)
                .status(MilestoneReport.Status.COMPLETED)
                .build();
    }

    @Test
    @DisplayName("기본 요약 조회 - FINAL 스냅샷 우선 조회")
    void getBasicSummary_FinalSnapshot() {
        // given
        when(basicSummaryRepository.findFinalByReportId(testReportId))
                .thenReturn(Optional.of(testBasicSummary));

        // when
        BasicSummaryResponse result = basicSummaryService.getBasicSummary(testReportId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalDistanceKm()).isEqualTo(26.6);
        assertThat(result.getAverageDurationSec()).isEqualTo(38.25);
        assertThat(result.getAverageDistanceKm()).isEqualTo(1.77);
        assertThat(result.getAverageSpeedKmh()).isEqualTo(42.3);
        assertThat(result.getAverageCruiseRatio()).isEqualTo(0.684);
        assertThat(result.getPeriodStart()).isEqualTo(LocalDate.of(2025, 8, 1));
        assertThat(result.getPeriodEnd()).isEqualTo(LocalDate.of(2025, 8, 28));
        assertThat(result.getReportId()).matches("u\\d+_r\\d+_\\d{8}");

        verify(basicSummaryRepository).findFinalByReportId(testReportId);
        verify(basicSummaryRepository, never()).findInterimByReportId(testReportId);
    }

    @Test
    @DisplayName("기본 요약 조회 - FINAL 없을 때 INTERIM 조회")
    void getBasicSummary_InterimSnapshot() {
        // given
        BasicSummary interimSummary = BasicSummary.builder()
                .reportId(testReportId)
                .userId(testUserId)
                .snapshotType(BasicSummary.SnapshotType.INTERIM)
                .totalDistanceKm(BigDecimal.valueOf(15.5))
                .averageDurationSec(BigDecimal.valueOf(30.0))
                .averageDistanceKm(BigDecimal.valueOf(1.5))
                .averageSpeedKmh(BigDecimal.valueOf(40.0))
                .averageCruiseRatio(BigDecimal.valueOf(0.65))
                .periodStart(LocalDate.of(2025, 8, 1))
                .periodEnd(LocalDate.of(2025, 8, 15))
                .build();

        when(basicSummaryRepository.findFinalByReportId(testReportId))
                .thenReturn(Optional.empty());
        when(basicSummaryRepository.findInterimByReportId(testReportId))
                .thenReturn(Optional.of(interimSummary));

        // when
        BasicSummaryResponse result = basicSummaryService.getBasicSummary(testReportId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalDistanceKm()).isEqualTo(15.5);
        assertThat(result.getAverageDurationSec()).isEqualTo(30.0);

        verify(basicSummaryRepository).findFinalByReportId(testReportId);
        verify(basicSummaryRepository).findInterimByReportId(testReportId);
    }

    @Test
    @DisplayName("기본 요약 조회 - 스냅샷 없음 예외")
    void getBasicSummary_NotFound() {
        // given
        when(basicSummaryRepository.findFinalByReportId(testReportId))
                .thenReturn(Optional.empty());
        when(basicSummaryRepository.findInterimByReportId(testReportId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> basicSummaryService.getBasicSummary(testReportId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("기본 통계를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("INTERIM 스냅샷 생성/갱신")
    void createOrUpdateInterimSnapshot() {
        // given
        DrivingAccumulatedStatsRepository.BasicSummaryProjection projection = 
                createMockProjection();

        when(milestoneReportRepository.findById(testReportId))
                .thenReturn(Optional.of(testMilestoneReport));
        when(drivingAccumulatedStatsRepository.getBasicSummaryByReportId(testReportId))
                .thenReturn(projection);

        // when
        basicSummaryService.createOrUpdateInterimSnapshot(testReportId);

        // then
        verify(basicSummaryRepository).deleteByReportIdAndSnapshotType(
                testReportId, BasicSummary.SnapshotType.INTERIM);
        verify(basicSummaryRepository).save(any(BasicSummary.class));
    }

    @Test
    @DisplayName("FINAL 스냅샷 생성")
    void createFinalSnapshot() {
        // given
        DrivingAccumulatedStatsRepository.BasicSummaryProjection projection = 
                createMockProjection();

        when(milestoneReportRepository.findById(testReportId))
                .thenReturn(Optional.of(testMilestoneReport));
        when(drivingAccumulatedStatsRepository.getBasicSummaryByReportId(testReportId))
                .thenReturn(projection);

        // when
        basicSummaryService.createFinalSnapshot(testReportId);

        // then
        verify(basicSummaryRepository, never()).deleteByReportIdAndSnapshotType(any(), any());
        verify(basicSummaryRepository).save(any(BasicSummary.class));
    }

    @Test
    @DisplayName("스냅샷 생성 - 마일스톤 리포트 없음 예외")
    void createSnapshot_MilestoneReportNotFound() {
        // given
        when(milestoneReportRepository.findById(testReportId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> basicSummaryService.createFinalSnapshot(testReportId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("마일스톤 리포트를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("스냅샷 생성 - 누적 통계 없음 예외")
    void createSnapshot_AccumulatedStatsNotFound() {
        // given
        when(milestoneReportRepository.findById(testReportId))
                .thenReturn(Optional.of(testMilestoneReport));
        when(drivingAccumulatedStatsRepository.getBasicSummaryByReportId(testReportId))
                .thenReturn(null);

        // when & then
        assertThatThrownBy(() -> basicSummaryService.createFinalSnapshot(testReportId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("누적 통계 데이터를 찾을 수 없습니다");
    }

    private DrivingAccumulatedStatsRepository.BasicSummaryProjection createMockProjection() {
        return new DrivingAccumulatedStatsRepository.BasicSummaryProjection() {
            @Override
            public Double getTotalDistanceKm() { return 26.6; }
            @Override
            public Double getAverageDurationSec() { return 38.25; }
            @Override
            public Double getAverageDistanceKm() { return 1.77; }
            @Override
            public Double getAverageSpeedKmh() { return 42.3; }
            @Override
            public Double getAverageCruiseRatio() { return 0.684; }
            @Override
            public LocalDate getPeriodStart() { return LocalDate.of(2025, 8, 1); }
            @Override
            public LocalDate getPeriodEnd() { return LocalDate.of(2025, 8, 28); }
        };
    }
}