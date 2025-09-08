package com.smooth.driving_analysis_service.reports.basic_summary;

import com.smooth.driving_analysis_service.pipeline.repository.DrivingAccumulatedStatsRepository;
import com.smooth.driving_analysis_service.reports.basic_summary.dto.BasicSummaryResponse;
import com.smooth.driving_analysis_service.reports.basic_summary.entity.BasicSummary;
import com.smooth.driving_analysis_service.reports.basic_summary.repository.BasicSummaryRepository;
import com.smooth.driving_analysis_service.reports.basic_summary.service.BasicSummaryServiceImpl;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BasicSummary 핵심 비즈니스 로직 단위테스트")
class BasicSummaryUnitTest {

    @Mock
    private BasicSummaryRepository basicSummaryRepository;

    @Mock
    private DrivingAccumulatedStatsRepository drivingAccumulatedStatsRepository;

    @Mock
    private MilestoneReportRepository milestoneReportRepository;

    @InjectMocks
    private BasicSummaryServiceImpl basicSummaryService;

    @Test
    @DisplayName("기본 요약 조회 - 정상 케이스")
    void getBasicSummary_Success() {
        // given
        Long reportId = 1L;
        BasicSummary mockSummary = createMockBasicSummary(reportId);
        
        when(basicSummaryRepository.findFinalByReportId(reportId))
                .thenReturn(Optional.of(mockSummary));

        // when
        BasicSummaryResponse result = basicSummaryService.getBasicSummary(reportId);

        // then
        assertThat(result.getTotalDistanceKm()).isEqualTo(50.0);
        assertThat(result.getAverageSpeedKmh()).isEqualTo(45.5);
        assertThat(result.getReportId()).matches("u\\d+_r\\d+_\\d{8}");
        
        verify(basicSummaryRepository).findFinalByReportId(reportId);
    }

    @Test
    @DisplayName("INTERIM 스냅샷 생성 - Mock 검증")
    void createInterimSnapshot_MockVerification() {
        // given
        Long reportId = 1L;
        MilestoneReport mockReport = createMockMilestoneReport(reportId);
        DrivingAccumulatedStatsRepository.BasicSummaryProjection mockProjection = createMockProjection();

        when(milestoneReportRepository.findById(reportId)).thenReturn(Optional.of(mockReport));
        when(drivingAccumulatedStatsRepository.getBasicSummaryByReportId(reportId)).thenReturn(mockProjection);

        // when
        basicSummaryService.createOrUpdateInterimSnapshot(reportId);

        // then - Mock 호출 검증
        verify(milestoneReportRepository).findById(reportId);
        verify(drivingAccumulatedStatsRepository).getBasicSummaryByReportId(reportId);
        verify(basicSummaryRepository).deleteByReportIdAndSnapshotType(reportId, BasicSummary.SnapshotType.INTERIM);
        verify(basicSummaryRepository).save(any(BasicSummary.class));
    }

    @Test
    @DisplayName("예외 처리 - 데이터 없음")
    void getBasicSummary_NotFound() {
        // given
        Long reportId = 999L;
        
        when(basicSummaryRepository.findFinalByReportId(reportId)).thenReturn(Optional.empty());
        when(basicSummaryRepository.findInterimByReportId(reportId)).thenReturn(Optional.empty());
        // 마일스톤 리포트도 없는 경우
        when(milestoneReportRepository.findById(reportId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> basicSummaryService.getBasicSummary(reportId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("해당 리포트가 존재하지 않습니다");
    }

    @Test
    @DisplayName("비즈니스 로직 검증 - reportId 포맷")
    void reportIdFormat_BusinessLogic() {
        // given
        Long reportId = 123L;
        Long userId = 456L;
        BasicSummary summary = BasicSummary.builder()
                .reportId(reportId)
                .userId(userId)
                .snapshotType(BasicSummary.SnapshotType.FINAL)
                .totalDistanceKm(BigDecimal.valueOf(30.0))
                .build();

        when(basicSummaryRepository.findFinalByReportId(reportId)).thenReturn(Optional.of(summary));

        // when
        BasicSummaryResponse result = basicSummaryService.getBasicSummary(reportId);

        // then - reportId 포맷 검증 (비즈니스 로직)
        String expectedPattern = "u456_r123_\\d{8}";
        assertThat(result.getReportId()).matches(expectedPattern);
    }

    private BasicSummary createMockBasicSummary(Long reportId) {
        return BasicSummary.builder()
                .id(1L)
                .reportId(reportId)
                .userId(12345L)
                .snapshotType(BasicSummary.SnapshotType.FINAL)
                .totalDistanceKm(BigDecimal.valueOf(50.0))
                .averageSpeedKmh(BigDecimal.valueOf(45.5))
                .averageCruiseRatio(BigDecimal.valueOf(0.75))
                .periodStart(LocalDate.of(2025, 8, 1))
                .periodEnd(LocalDate.of(2025, 8, 31))
                .build();
    }

    private MilestoneReport createMockMilestoneReport(Long reportId) {
        return MilestoneReport.builder()
                .id(reportId)
                .userId(12345L)
                .build();
    }

    private DrivingAccumulatedStatsRepository.BasicSummaryProjection createMockProjection() {
        return new DrivingAccumulatedStatsRepository.BasicSummaryProjection() {
            @Override
            public Double getTotalDistanceKm() { return 50.0; }
            @Override
            public Double getAverageDurationSec() { return 1800.0; }
            @Override
            public Double getAverageDistanceKm() { return 10.0; }
            @Override
            public Double getAverageSpeedKmh() { return 45.5; }
            @Override
            public Double getAverageCruiseRatio() { return 0.75; }
            @Override
            public LocalDate getPeriodStart() { return LocalDate.of(2025, 8, 1); }
            @Override
            public LocalDate getPeriodEnd() { return LocalDate.of(2025, 8, 31); }
        };
    }
}