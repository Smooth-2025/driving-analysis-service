package com.smooth.driving_analysis_service.reports.basic_summary.service;

import com.smooth.driving_analysis_service.reports.basic_summary.dto.BasicSummaryResponse;
import com.smooth.driving_analysis_service.reports.basic_summary.entity.BasicSummary;
import com.smooth.driving_analysis_service.reports.basic_summary.repository.BasicSummaryRepository;
import com.smooth.driving_analysis_service.reports.basic_summary.repository.DrivingAccumulatedStatsRepository;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
        when(projection.getTotalDistanceKm()).thenReturn(26.6);
        when(projection.getAverageDurationSec()).thenReturn(38.25);
        when(projection.getAverageDistanceKm()).thenReturn(1.77);
        when(projection.getAverageSpeedKmh()).thenReturn(42.3);
        when(projection.getAverageCruiseRatio()).thenReturn(0.684);
        when(projection.getPeriodStart()).thenReturn(LocalDate.of(2025, 8, 1));
        when(projection.getPeriodEnd()).thenReturn(LocalDate.of(2025, 8, 28));
        
        // when
        basicSummaryService.createOrUpdateInterimSnapshot(reportId);
        
        // then
        verify(basicSummaryRepository).deleteByReportIdAndSnapshotType(
                reportId, BasicSummary.SnapshotType.INTERIM);
        verify(basicSummaryRepository).save(any(BasicSummary.class));
    }
}