package com.smooth.driving_analysis_service.reports.basic_summary.service;

import com.smooth.driving_analysis_service.reports.basic_summary.dto.BasicSummaryResponse;
import com.smooth.driving_analysis_service.reports.basic_summary.entity.BasicSummary;
import com.smooth.driving_analysis_service.reports.basic_summary.repository.BasicSummaryRepository;
<<<<<<< HEAD
import com.smooth.driving_analysis_service.reports.basic_summary.repository.DrivingAccumulatedStatsRepository;
=======
import com.smooth.driving_analysis_service.pipeline.repository.DrivingAccumulatedStatsRepository;
>>>>>>> temp-dev
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
<<<<<<< HEAD
=======
import org.mockito.ArgumentCaptor;
>>>>>>> temp-dev
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

<<<<<<< HEAD
=======
import java.math.BigDecimal;
>>>>>>> temp-dev
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
<<<<<<< HEAD
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasicSummaryServiceTest {
    
=======
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BasicSummaryService 테스트")
class BasicSummaryServiceTest {

>>>>>>> temp-dev
    @Mock
    private BasicSummaryRepository basicSummaryRepository;
    
    @Mock
    private DrivingAccumulatedStatsRepository drivingAccumulatedStatsRepository;
    
    @Mock
    private MilestoneReportRepository milestoneReportRepository;
<<<<<<< HEAD
    
    @InjectMocks
    private BasicSummaryServiceImpl basicSummaryService;
    
    @Test
    @DisplayName("FINAL 스냅샷이 있으면 FINAL 스냅샷을 반환한다")
    void getBasicSummary_WithFinalSnapshot_ReturnsFinalSnapshot() {
        // given
        Long reportId = 1L;
        BasicSummary finalSnapshot = BasicSummary.builder()
                .reportId(reportId)
                .userId(12345L)
                .totalDistanceKm(26.6)
                .periodStart(LocalDate.of(2025, 8, 1))
                .periodEnd(LocalDate.of(2025, 8, 28))
                .averageDurationSec(38.25)
                .averageDistanceKm(1.77)
                .averageSpeedKmh(42.3)
                .averageCruiseRatio(0.684)
                .snapshotType(BasicSummary.SnapshotType.FINAL)
                .build();
        
        when(basicSummaryRepository.findFinalByReportId(reportId))
                .thenReturn(Optional.of(finalSnapshot));
        
        // when
        BasicSummaryResponse response = basicSummaryService.getBasicSummary(reportId);
        
        // then
        assertThat(response.getTotalDistanceKm()).isEqualTo(26.6);
        assertThat(response.getAverageDurationSec()).isEqualTo(38.25);
        assertThat(response.getAverageDistanceKm()).isEqualTo(1.77);
        assertThat(response.getAverageSpeedKmh()).isEqualTo(42.3);
        assertThat(response.getAverageCruiseRatio()).isEqualTo(0.684);
        assertThat(response.getPeriodStart()).isEqualTo(LocalDate.of(2025, 8, 1));
        assertThat(response.getPeriodEnd()).isEqualTo(LocalDate.of(2025, 8, 28));
        
        verify(basicSummaryRepository).findFinalByReportId(reportId);
        verify(basicSummaryRepository, never()).findInterimByReportId(any());
    }
    
    @Test
    @DisplayName("INTERIM 스냅샷 생성 시 기존 INTERIM을 삭제하고 새로 생성한다")
    void createOrUpdateInterimSnapshot_DeletesExistingAndCreatesNew() {
        // given
        Long reportId = 1L;
        Long userId = 12345L;
        
        MilestoneReport milestoneReport = MilestoneReport.builder()
                .id(reportId)
                .userId(userId)
                .build();
        
        DrivingAccumulatedStatsRepository.BasicSummaryProjection projection = 
                mock(DrivingAccumulatedStatsRepository.BasicSummaryProjection.class);
        
        when(milestoneReportRepository.findById(reportId))
                .thenReturn(Optional.of(milestoneReport));
        when(drivingAccumulatedStatsRepository.getBasicSummaryByReportId(reportId))
                .thenReturn(projection);
=======

    @InjectMocks
    private BasicSummaryServiceImpl basicSummaryService;

    @Test
    @DisplayName("INTERIM 리포트 생성")
    void testGenerateInterimReport() {
        // Given
        Long reportId = 1L;
        Long userId = 12345L;
        
        when(milestoneReportRepository.findById(reportId))
                .thenReturn(Optional.of(createMockMilestoneReport(reportId, userId)));
        when(drivingAccumulatedStatsRepository.getBasicSummaryByReportId(reportId))
                .thenReturn(createMockProjection());

        // When
        basicSummaryService.generateInterimReport(reportId, userId);

        // Then
        verify(basicSummaryRepository).deleteByReportIdAndSnapshotType(reportId, BasicSummary.SnapshotType.INTERIM);
        
        ArgumentCaptor<BasicSummary> captor = ArgumentCaptor.forClass(BasicSummary.class);
        verify(basicSummaryRepository).save(captor.capture());
        
        BasicSummary saved = captor.getValue();
        assertThat(saved.getReportId()).isEqualTo(reportId);
        assertThat(saved.getSnapshotType()).isEqualTo(BasicSummary.SnapshotType.INTERIM);
        assertThat(saved.getTotalDistanceKm()).isEqualTo(BigDecimal.valueOf(26.6));
    }

    @Test
    @DisplayName("FINAL 리포트 생성")
    void testGenerateFinalReport() {
        // Given
        Long reportId = 1L;
        Long userId = 12345L;
        
        when(milestoneReportRepository.findById(reportId))
                .thenReturn(Optional.of(createMockMilestoneReport(reportId, userId)));
        when(drivingAccumulatedStatsRepository.getBasicSummaryByReportId(reportId))
                .thenReturn(createMockProjection());

        // When
        basicSummaryService.generateFinalReport(reportId, userId);

        // Then
        ArgumentCaptor<BasicSummary> captor = ArgumentCaptor.forClass(BasicSummary.class);
        verify(basicSummaryRepository).save(captor.capture());
        
        BasicSummary saved = captor.getValue();
        assertThat(saved.getReportId()).isEqualTo(reportId);
        assertThat(saved.getSnapshotType()).isEqualTo(BasicSummary.SnapshotType.FINAL);
        assertThat(saved.getTotalDistanceKm()).isEqualTo(BigDecimal.valueOf(26.6));
    }

    @Test
    @DisplayName("기본 요약 조회 - FINAL 우선")
    void testGetBasicSummary_FinalFirst() {
        // Given
        Long reportId = 1L;
        
        BasicSummary finalSummary = createMockSummary(reportId, BasicSummary.SnapshotType.FINAL);
        
        when(basicSummaryRepository.findFinalByReportId(reportId))
                .thenReturn(Optional.of(finalSummary));

        // When
        BasicSummaryResponse response = basicSummaryService.getBasicSummary(reportId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTotalDistanceKm()).isEqualTo(26.6);
        
        // INTERIM은 조회하지 않음
        verify(basicSummaryRepository, never()).findInterimByReportId(reportId);
    }

    @Test
    @DisplayName("기본 요약 조회 - FINAL 없으면 INTERIM")
    void testGetBasicSummary_InterimFallback() {
        // Given
        Long reportId = 1L;
        
        BasicSummary interimSummary = createMockSummary(reportId, BasicSummary.SnapshotType.INTERIM);
        
        when(basicSummaryRepository.findFinalByReportId(reportId))
                .thenReturn(Optional.empty());
        when(basicSummaryRepository.findInterimByReportId(reportId))
                .thenReturn(Optional.of(interimSummary));

        // When
        BasicSummaryResponse response = basicSummaryService.getBasicSummary(reportId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTotalDistanceKm()).isEqualTo(26.6);
    }

    @Test
    @DisplayName("기본 요약 조회 - 데이터 없음")
    void testGetBasicSummary_NotFound() {
        // Given
        Long reportId = 1L;
        
        when(basicSummaryRepository.findFinalByReportId(reportId))
                .thenReturn(Optional.empty());
        when(basicSummaryRepository.findInterimByReportId(reportId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> basicSummaryService.getBasicSummary(reportId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("기본 통계를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("마일스톤 리포트 없음 예외")
    void testGenerateReport_NoMilestoneReport() {
        // Given
        Long reportId = 1L;
        Long userId = 12345L;
        
        when(milestoneReportRepository.findById(reportId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> basicSummaryService.generateInterimReport(reportId, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("마일스톤 리포트를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("누적 통계 없음 예외")
    void testGenerateReport_NoStats() {
        // Given
        Long reportId = 1L;
        Long userId = 12345L;
        
        when(milestoneReportRepository.findById(reportId))
                .thenReturn(Optional.of(createMockMilestoneReport(reportId, userId)));
        when(drivingAccumulatedStatsRepository.getBasicSummaryByReportId(reportId))
                .thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> basicSummaryService.generateInterimReport(reportId, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("누적 통계 데이터를 찾을 수 없습니다");
    }

    private MilestoneReport createMockMilestoneReport(Long reportId, Long userId) {
        return MilestoneReport.builder()
                .id(reportId)
                .userId(userId)
                .build();
    }

    private DrivingAccumulatedStatsRepository.BasicSummaryProjection createMockProjection() {
        DrivingAccumulatedStatsRepository.BasicSummaryProjection projection = 
                mock(DrivingAccumulatedStatsRepository.BasicSummaryProjection.class);
>>>>>>> temp-dev
        when(projection.getTotalDistanceKm()).thenReturn(26.6);
        when(projection.getAverageDurationSec()).thenReturn(38.25);
        when(projection.getAverageDistanceKm()).thenReturn(1.77);
        when(projection.getAverageSpeedKmh()).thenReturn(42.3);
        when(projection.getAverageCruiseRatio()).thenReturn(0.684);
        when(projection.getPeriodStart()).thenReturn(LocalDate.of(2025, 8, 1));
        when(projection.getPeriodEnd()).thenReturn(LocalDate.of(2025, 8, 28));
<<<<<<< HEAD
        
        // when
        basicSummaryService.createOrUpdateInterimSnapshot(reportId);
        
        // then
        verify(basicSummaryRepository).deleteByReportIdAndSnapshotType(
                reportId, BasicSummary.SnapshotType.INTERIM);
        verify(basicSummaryRepository).save(any(BasicSummary.class));
=======
        return projection;
    }

    private BasicSummary createMockSummary(Long reportId, BasicSummary.SnapshotType snapshotType) {
        return BasicSummary.builder()
                .id(1L)
                .reportId(reportId)
                .userId(12345L)
                .snapshotType(snapshotType)
                .totalDistanceKm(BigDecimal.valueOf(26.6))
                .periodStart(LocalDate.of(2025, 8, 1))
                .periodEnd(LocalDate.of(2025, 8, 28))
                .averageDurationSec(BigDecimal.valueOf(38.25))
                .averageDistanceKm(BigDecimal.valueOf(1.77))
                .averageSpeedKmh(BigDecimal.valueOf(42.3))
                .averageCruiseRatio(BigDecimal.valueOf(0.684))
                .build();
>>>>>>> temp-dev
    }
}