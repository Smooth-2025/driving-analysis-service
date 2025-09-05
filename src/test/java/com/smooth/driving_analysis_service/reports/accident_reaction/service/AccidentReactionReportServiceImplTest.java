package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.AccidentReactionBasicMetricsDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.AccidentReactionReportResponseDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.repository.AccidentReactionMetricRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccidentReactionReportServiceImplTest {

    @Mock
    private AccidentReactionMetricRepository accidentReactionMetricRepository;

    @InjectMocks
    private AccidentReactionReportServiceImpl accidentReactionReportService;

    @BeforeEach
    void setUp() {
        // 테스트 설정
    }

    @Test
    @DisplayName("Task 1: 기본 반응 지표 조회 - 정상 케이스")
    void getBasicMetrics_Success() {
        // Given
        String reportId = "123";
        Object[] mockResult = {12L, 1.7, 0.38, 0.12}; // receivedAlertCount, avgReactionSec, brakeOrStopRatio, avoidRatio
        
        when(accidentReactionMetricRepository.getBasicMetricsByReportId(123L))
                .thenReturn(mockResult);

        // When
        AccidentReactionBasicMetricsDto result = accidentReactionReportService.getBasicMetrics(reportId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReceivedAlertCount()).isEqualTo(12);
        assertThat(result.getAvgReactionSec()).isEqualTo(1.7);
        assertThat(result.getBrakeOrStopRatio()).isEqualTo(0.38);
        assertThat(result.getAvoidRatio()).isEqualTo(0.12);
    }

    @Test
    @DisplayName("Task 1: 기본 반응 지표 조회 - 데이터 없음")
    void getBasicMetrics_NoData() {
        // Given
        String reportId = "999";
        when(accidentReactionMetricRepository.getBasicMetricsByReportId(999L))
                .thenReturn(null);

        // When
        AccidentReactionBasicMetricsDto result = accidentReactionReportService.getBasicMetrics(reportId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReceivedAlertCount()).isEqualTo(0);
        assertThat(result.getAvgReactionSec()).isEqualTo(0.0);
        assertThat(result.getBrakeOrStopRatio()).isEqualTo(0.0);
        assertThat(result.getAvoidRatio()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Task 1: 기본 반응 지표 조회 - null 값 처리")
    void getBasicMetrics_WithNullValues() {
        // Given
        String reportId = "123";
        Object[] mockResult = {5L, null, 0.2, null}; // 일부 null 값
        
        when(accidentReactionMetricRepository.getBasicMetricsByReportId(123L))
                .thenReturn(mockResult);

        // When
        AccidentReactionBasicMetricsDto result = accidentReactionReportService.getBasicMetrics(reportId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReceivedAlertCount()).isEqualTo(5);
        assertThat(result.getAvgReactionSec()).isEqualTo(0.0); // null -> 0.0
        assertThat(result.getBrakeOrStopRatio()).isEqualTo(0.2);
        assertThat(result.getAvoidRatio()).isEqualTo(0.0); // null -> 0.0
    }

    @Test
    @DisplayName("Task 1: 기본 반응 지표 조회 - 잘못된 reportId 형식")
    void getBasicMetrics_InvalidReportIdFormat() {
        // Given
        String reportId = "invalid";

        // When
        AccidentReactionBasicMetricsDto result = accidentReactionReportService.getBasicMetrics(reportId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReceivedAlertCount()).isEqualTo(0);
        assertThat(result.getAvgReactionSec()).isEqualTo(0.0);
        assertThat(result.getBrakeOrStopRatio()).isEqualTo(0.0);
        assertThat(result.getAvoidRatio()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("전체 리포트 조회 - 정상 케이스")
    void getFullReport_Success() {
        // Given
        String reportId = "123";
        Object[] mockResult = {8L, 2.1, 0.5, 0.25};
        
        when(accidentReactionMetricRepository.getBasicMetricsByReportId(123L))
                .thenReturn(mockResult);

        // When
        AccidentReactionReportResponseDto result = accidentReactionReportService.getFullReport(reportId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReportId()).isEqualTo("123");
        assertThat(result.getReceivedAlertCount()).isEqualTo(8);
        assertThat(result.getAvgReactionSec()).isEqualTo(2.1);
        assertThat(result.getBrakeOrStopRatio()).isEqualTo(0.5);
        assertThat(result.getAvoidRatio()).isEqualTo(0.25);
    }
}