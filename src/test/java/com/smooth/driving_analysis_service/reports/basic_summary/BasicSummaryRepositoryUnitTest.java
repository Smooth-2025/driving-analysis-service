package com.smooth.driving_analysis_service.reports.basic_summary;

import com.smooth.driving_analysis_service.reports.basic_summary.entity.BasicSummary;
import com.smooth.driving_analysis_service.reports.basic_summary.repository.BasicSummaryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BasicSummaryRepository Mock 테스트")
class BasicSummaryRepositoryUnitTest {

    @Mock
    private BasicSummaryRepository basicSummaryRepository;

    @Test
    @DisplayName("FINAL 스냅샷 조회 - Mock 검증")
    void findFinalByReportId() {
        // given
        Long reportId = 1L;
        BasicSummary mockSummary = createBasicSummary(reportId, BasicSummary.SnapshotType.FINAL);
        
        when(basicSummaryRepository.findFinalByReportId(reportId))
                .thenReturn(Optional.of(mockSummary));

        // when
        Optional<BasicSummary> result = basicSummaryRepository.findFinalByReportId(reportId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getSnapshotType()).isEqualTo(BasicSummary.SnapshotType.FINAL);
        verify(basicSummaryRepository).findFinalByReportId(reportId);
    }

    @Test
    @DisplayName("INTERIM 스냅샷 조회 - Mock 검증")
    void findInterimByReportId() {
        // given
        Long reportId = 2L;
        BasicSummary mockSummary = createBasicSummary(reportId, BasicSummary.SnapshotType.INTERIM);
        
        when(basicSummaryRepository.findInterimByReportId(reportId))
                .thenReturn(Optional.of(mockSummary));

        // when
        Optional<BasicSummary> result = basicSummaryRepository.findInterimByReportId(reportId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getSnapshotType()).isEqualTo(BasicSummary.SnapshotType.INTERIM);
        verify(basicSummaryRepository).findInterimByReportId(reportId);
    }

    @Test
    @DisplayName("존재하지 않는 데이터 조회")
    void findByNonExistentReportId() {
        // given
        Long reportId = 999L;
        
        when(basicSummaryRepository.findFinalByReportId(reportId))
                .thenReturn(Optional.empty());

        // when
        Optional<BasicSummary> result = basicSummaryRepository.findFinalByReportId(reportId);

        // then
        assertThat(result).isEmpty();
        verify(basicSummaryRepository).findFinalByReportId(reportId);
    }

    @Test
    @DisplayName("삭제 메서드 호출 검증")
    void deleteByReportIdAndSnapshotType() {
        // given
        Long reportId = 3L;
        BasicSummary.SnapshotType snapshotType = BasicSummary.SnapshotType.INTERIM;

        // when
        basicSummaryRepository.deleteByReportIdAndSnapshotType(reportId, snapshotType);

        // then
        verify(basicSummaryRepository).deleteByReportIdAndSnapshotType(reportId, snapshotType);
    }

    private BasicSummary createBasicSummary(Long reportId, BasicSummary.SnapshotType snapshotType) {
        return BasicSummary.builder()
                .reportId(reportId)
                .userId(12345L)
                .snapshotType(snapshotType)
                .totalDistanceKm(BigDecimal.valueOf(50.0))
                .averageSpeedKmh(BigDecimal.valueOf(45.0))
                .averageCruiseRatio(BigDecimal.valueOf(0.7))
                .periodStart(LocalDate.of(2025, 8, 1))
                .periodEnd(LocalDate.of(2025, 8, 31))
                .build();
    }
}