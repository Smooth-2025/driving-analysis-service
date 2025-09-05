package com.smooth.driving_analysis_service.reports.basic_summary.service;

import com.smooth.driving_analysis_service.reports.basic_summary.dto.BasicSummaryResponse;
import com.smooth.driving_analysis_service.reports.basic_summary.entity.BasicSummary;
import com.smooth.driving_analysis_service.reports.basic_summary.repository.BasicSummaryRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BasicSummaryService 테스트")
class BasicSummaryServiceTest {

    @Mock
    private BasicSummaryRepository basicSummaryRepository;

    @InjectMocks
    private BasicSummaryServiceImpl basicSummaryService;

    @Test
    @DisplayName("INTERIM 리포트 생성 - 새로 생성")
    void testGenerateInterimReport_New() {
        // Given
        Long reportId = 1L;
        Long userId = 12345L;
        
        when(basicSummaryRepository.findByReportIdAndSnapshotType(reportId, BasicSummary.SnapshotType.INTERIM))
                .thenReturn(Optional.empty());
        when(basicSummaryRepository.calculateSummaryByReportId(reportId))
                .thenReturn(createMockProjection());

        // When
        basicSummaryService.generateInterimReport(reportId, userId);

        // Then
        ArgumentCaptor<BasicSummary> captor = ArgumentCaptor.forClass(BasicSummary.class);
        verify(basicSummaryRepository).save(captor.capture());
        
        BasicSummary saved = captor.getValue();
        assertThat(saved.getReportId()).isEqualTo(reportId);
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getSnapshotType()).isEqualTo(BasicSummary.SnapshotType.INTERIM);
        assertThat(saved.getTotalDistanceKm()).isEqualTo(BigDecimal.valueOf(26.60));
        assertThat(saved.getAverageSpeedKmh()).isEqualTo(BigDecimal.valueOf(42.3));
    }

    @Test
    @DisplayName("INTERIM 리포트 생성 - 기존 갱신")
    void testGenerateInterimReport_Update() {
        // Given
        Long reportId = 1L;
        Long userId = 12345L;
        
        BasicSummary existing = BasicSummary.builder()
                .id(10L)
                .reportId(reportId)
                .userId(userId)
                .snapshotType(BasicSummary.SnapshotType.INTERIM)
                .totalDistanceKm(BigDecimal.valueOf(20.0))
                .build();
        
        when(basicSummaryRepository.findByReportIdAndSnapshotType(reportId, BasicSummary.SnapshotType.INTERIM))
                .thenReturn(Optional.of(existing));
        when(basicSummaryRepository.calculateSummaryByReportId(reportId))
                .thenReturn(createMockProjection());

        // When
        basicSummaryService.generateInterimReport(reportId, userId);

        // Then
        ArgumentCaptor<BasicSummary> captor = ArgumentCaptor.forClass(BasicSummary.class);
        verify(basicSummaryRepository).save(captor.capture());
        
        BasicSummary saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(10L); // 기존 ID 유지
        assertThat(saved.getTotalDistanceKm()).isEqualTo(BigDecimal.valueOf(26.60)); // 새로운 값으로 갱신
    }

    @Test
    @DisplayName("FINAL 리포트 생성")
    void testGenerateFinalReport() {
        // Given
        Long reportId = 1L;
        Long userId = 12345L;
        
        when(basicSummaryRepository.calculateSummaryByReportId(reportId))
                .thenReturn(createMockProjection());

        // When
        basicSummaryService.generateFinalReport(reportId, userId);

        // Then
        ArgumentCaptor<BasicSummary> captor = ArgumentCaptor.forClass(BasicSummary.class);
        verify(basicSummaryRepository).save(captor.capture());
        
        BasicSummary saved = captor.getValue();
        assertThat(saved.getReportId()).isEqualTo(reportId);
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getSnapshotType()).isEqualTo(BasicSummary.SnapshotType.FINAL);
        assertThat(saved.getTotalDistanceKm()).isEqualTo(BigDecimal.valueOf(26.60));
        assertThat(saved.getAverageSpeedKmh()).isEqualTo(BigDecimal.valueOf(42.3));
    }

    @Test
    @DisplayName("누적 통계 없는 경우 처리")
    void testGenerateReport_NoStats() {
        // Given
        Long reportId = 1L;
        Long userId = 12345L;
        
        when(basicSummaryRepository.findByReportIdAndSnapshotType(reportId, BasicSummary.SnapshotType.INTERIM))
                .thenReturn(Optional.empty());
        when(basicSummaryRepository.calculateSummaryByReportId(reportId))
                .thenReturn(null);

        // When
        basicSummaryService.generateInterimReport(reportId, userId);

        // Then
        ArgumentCaptor<BasicSummary> captor = ArgumentCaptor.forClass(BasicSummary.class);
        verify(basicSummaryRepository).save(captor.capture());
        
        BasicSummary saved = captor.getValue();
        assertThat(saved.getReportId()).isEqualTo(reportId);
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getSnapshotType()).isEqualTo(BasicSummary.SnapshotType.INTERIM);
        // 데이터가 없어도 엔티티는 저장됨 (기본값들)
    }

    @Test
    @DisplayName("기본 요약 조회 - FINAL 우선")
    void testGetBasicSummary_FinalFirst() {
        // Given
        Long reportId = 1L;
        
        BasicSummary finalSummary = createMockSummary(reportId, BasicSummary.SnapshotType.FINAL);
        BasicSummary interimSummary = createMockSummary(reportId, BasicSummary.SnapshotType.INTERIM);
        
        when(basicSummaryRepository.findByReportIdAndSnapshotType(reportId, BasicSummary.SnapshotType.FINAL))
                .thenReturn(Optional.of(finalSummary));

        // When
        BasicSummaryResponse response = basicSummaryService.getBasicSummary(reportId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getReportId()).isEqualTo("1");
        assertThat(response.getTotalDistanceKm()).isEqualTo(BigDecimal.valueOf(26.60));
        
        // INTERIM은 조회하지 않음
        verify(basicSummaryRepository, never())
                .findByReportIdAndSnapshotType(reportId, BasicSummary.SnapshotType.INTERIM);
    }

    @Test
    @DisplayName("기본 요약 조회 - FINAL 없으면 INTERIM")
    void testGetBasicSummary_InterimFallback() {
        // Given
        Long reportId = 1L;
        
        BasicSummary interimSummary = createMockSummary(reportId, BasicSummary.SnapshotType.INTERIM);
        
        when(basicSummaryRepository.findByReportIdAndSnapshotType(reportId, BasicSummary.SnapshotType.FINAL))
                .thenReturn(Optional.empty());
        when(basicSummaryRepository.findByReportIdAndSnapshotType(reportId, BasicSummary.SnapshotType.INTERIM))
                .thenReturn(Optional.of(interimSummary));

        // When
        BasicSummaryResponse response = basicSummaryService.getBasicSummary(reportId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getReportId()).isEqualTo("1");
        assertThat(response.getTotalDistanceKm()).isEqualTo(BigDecimal.valueOf(26.60));
    }

    @Test
    @DisplayName("기본 요약 조회 - 데이터 없음")
    void testGetBasicSummary_NotFound() {
        // Given
        Long reportId = 1L;
        
        when(basicSummaryRepository.findByReportIdAndSnapshotType(reportId, BasicSummary.SnapshotType.FINAL))
                .thenReturn(Optional.empty());
        when(basicSummaryRepository.findByReportIdAndSnapshotType(reportId, BasicSummary.SnapshotType.INTERIM))
                .thenReturn(Optional.empty());

        // When
        BasicSummaryResponse response = basicSummaryService.getBasicSummary(reportId);

        // Then
        assertThat(response).isNull();
    }

    @Test
    @DisplayName("날짜 파싱 실패 처리")
    void testDateParsingFailure() {
        // Given
        Long reportId = 1L;
        Long userId = 12345L;
        
        BasicSummaryRepository.BasicSummaryProjection projection = mock(BasicSummaryRepository.BasicSummaryProjection.class);
        when(projection.getTotalDistanceKm()).thenReturn(26.6);
        when(projection.getAverageDurationSec()).thenReturn(38.25);
        when(projection.getAverageDistanceKm()).thenReturn(1.77);
        when(projection.getAverageSpeedKmh()).thenReturn(42.3);
        when(projection.getAverageCruiseRatio()).thenReturn(0.684);
        when(projection.getPeriodStart()).thenReturn("invalid-date");
        when(projection.getPeriodEnd()).thenReturn("2025-08-28");
        
        when(basicSummaryRepository.findByReportIdAndSnapshotType(reportId, BasicSummary.SnapshotType.INTERIM))
                .thenReturn(Optional.empty());
        when(basicSummaryRepository.calculateSummaryByReportId(reportId))
                .thenReturn(projection);

        // When
        basicSummaryService.generateInterimReport(reportId, userId);

        // Then
        ArgumentCaptor<BasicSummary> captor = ArgumentCaptor.forClass(BasicSummary.class);
        verify(basicSummaryRepository).save(captor.capture());
        
        BasicSummary saved = captor.getValue();
        assertThat(saved.getPeriodStart()).isNull(); // 파싱 실패 시 null
        assertThat(saved.getPeriodEnd()).isEqualTo(LocalDate.of(2025, 8, 28)); // 정상 파싱
    }

    private BasicSummaryRepository.BasicSummaryProjection createMockProjection() {
        BasicSummaryRepository.BasicSummaryProjection projection = mock(BasicSummaryRepository.BasicSummaryProjection.class);
        when(projection.getTotalDistanceKm()).thenReturn(26.6);
        when(projection.getAverageDurationSec()).thenReturn(38.25);
        when(projection.getAverageDistanceKm()).thenReturn(1.77);
        when(projection.getAverageSpeedKmh()).thenReturn(42.3);
        when(projection.getAverageCruiseRatio()).thenReturn(0.684);
        when(projection.getPeriodStart()).thenReturn("2025-08-01");
        when(projection.getPeriodEnd()).thenReturn("2025-08-28");
        return projection;
    }

    private BasicSummary createMockSummary(Long reportId, BasicSummary.SnapshotType snapshotType) {
        return BasicSummary.builder()
                .id(1L)
                .reportId(reportId)
                .userId(12345L)
                .snapshotType(snapshotType)
                .totalDistanceKm(BigDecimal.valueOf(26.60))
                .periodStart(LocalDate.of(2025, 8, 1))
                .periodEnd(LocalDate.of(2025, 8, 28))
                .averageDurationSec(BigDecimal.valueOf(38.25))
                .averageDistanceKm(BigDecimal.valueOf(1.77))
                .averageSpeedKmh(BigDecimal.valueOf(42.3))
                .averageCruiseRatio(BigDecimal.valueOf(0.684))
                .build();
    }
}