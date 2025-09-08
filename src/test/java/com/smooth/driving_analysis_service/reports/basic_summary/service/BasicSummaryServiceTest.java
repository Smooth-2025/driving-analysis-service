package com.smooth.driving_analysis_service.reports.basic_summary.service;

import com.smooth.driving_analysis_service.reports.basic_summary.dto.BasicSummaryResponse;
import com.smooth.driving_analysis_service.reports.basic_summary.entity.BasicSummary;
import com.smooth.driving_analysis_service.reports.basic_summary.repository.BasicSummaryRepository;
import com.smooth.driving_analysis_service.pipeline.repository.DrivingAccumulatedStatsRepository;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BasicSummaryService 테스트")
class BasicSummaryServiceTest {

    @Mock
    private BasicSummaryRepository basicSummaryRepository;
    
    @Mock
    private DrivingAccumulatedStatsRepository drivingAccumulatedStatsRepository;
    
    @Mock
    private MilestoneReportRepository milestoneReportRepository;

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
        basicSummaryService.createOrUpdateInterimSnapshot(reportId);

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
        basicSummaryService.createFinalSnapshot(reportId);

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
        // 마일스톤 리포트도 없는 경우
        when(milestoneReportRepository.findById(reportId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> basicSummaryService.getBasicSummary(reportId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("해당 리포트가 존재하지 않습니다");
    }

    @Test
    @DisplayName("마일스톤 리포트 없음 예외")
    void testGenerateReport_NoMilestoneReport() {
        // Given
        Long reportId = 1L;
        
        when(milestoneReportRepository.findById(reportId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> basicSummaryService.createFinalSnapshot(reportId))
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
        assertThatThrownBy(() -> basicSummaryService.createFinalSnapshot(reportId))
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
    }
}