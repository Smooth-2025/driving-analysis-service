package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.AccidentReactionBenchmarkDto;
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
class AccidentReactionReportServiceImplTask2Test {

    @Mock
    private AccidentReactionMetricRepository accidentReactionMetricRepository;

    @InjectMocks
    private AccidentReactionReportServiceImpl accidentReactionReportService;

    @BeforeEach
    void setUp() {
        // 테스트 설정
    }

    @Test
    @DisplayName("Task 2: 벤치마크 비교 - 내가 더 빠른 경우")
    void getBenchmark_FasterThanAverage() {
        // Given
        String reportId = "123";
        Object[] basicMetricsResult = {8L, 1.2, 0.5, 0.25}; // 내 평균: 1.2초
        Double globalAverage = 1.8; // 일반 평균: 1.8초
        
        when(accidentReactionMetricRepository.getBasicMetricsByReportId(123L))
                .thenReturn(basicMetricsResult);
        when(accidentReactionMetricRepository.getGlobalAverageReactionTime())
                .thenReturn(globalAverage);

        // When
        AccidentReactionBenchmarkDto result = accidentReactionReportService.getBenchmark(reportId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDeltaSec()).isEqualTo(0.6); // 1.8 - 1.2 = 0.6 (더 빠름)
        assertThat(result.getChart().getLabels()).containsExactly("일반 운전자", "내 주행");
        assertThat(result.getChart().getValuesSec()).containsExactly(1.8, 1.2);
    }

    @Test
    @DisplayName("Task 2: 벤치마크 비교 - 내가 더 느린 경우")
    void getBenchmark_SlowerThanAverage() {
        // Given
        String reportId = "123";
        Object[] basicMetricsResult = {5L, 2.3, 0.4, 0.2}; // 내 평균: 2.3초
        Double globalAverage = 1.5; // 일반 평균: 1.5초
        
        when(accidentReactionMetricRepository.getBasicMetricsByReportId(123L))
                .thenReturn(basicMetricsResult);
        when(accidentReactionMetricRepository.getGlobalAverageReactionTime())
                .thenReturn(globalAverage);

        // When
        AccidentReactionBenchmarkDto result = accidentReactionReportService.getBenchmark(reportId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDeltaSec()).isEqualTo(-0.8); // 1.5 - 2.3 = -0.8 (더 느림)
        assertThat(result.getChart().getLabels()).containsExactly("일반 운전자", "내 주행");
        assertThat(result.getChart().getValuesSec()).containsExactly(1.5, 2.3);
    }

    @Test
    @DisplayName("Task 2: 벤치마크 비교 - 전체 데이터 없음")
    void getBenchmark_NoGlobalData() {
        // Given
        String reportId = "123";
        Object[] basicMetricsResult = {3L, 1.5, 0.3, 0.1};
        
        when(accidentReactionMetricRepository.getBasicMetricsByReportId(123L))
                .thenReturn(basicMetricsResult);
        when(accidentReactionMetricRepository.getGlobalAverageReactionTime())
                .thenReturn(null); // 전체 데이터 없음

        // When
        AccidentReactionBenchmarkDto result = accidentReactionReportService.getBenchmark(reportId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDeltaSec()).isEqualTo(0.5); // 2.0(기본값) - 1.5 = 0.5
        assertThat(result.getChart().getValuesSec()).containsExactly(2.0, 1.5);
    }

    @Test
    @DisplayName("Task 2: 전체 리포트 조회 - Task 1 + Task 2 통합")
    void getFullReport_WithBenchmark() {
        // Given
        String reportId = "123";
        Object[] basicMetricsResult = {10L, 1.7, 0.4, 0.3};
        Double globalAverage = 1.4;
        
        when(accidentReactionMetricRepository.getBasicMetricsByReportId(123L))
                .thenReturn(basicMetricsResult);
        when(accidentReactionMetricRepository.getGlobalAverageReactionTime())
                .thenReturn(globalAverage);

        // When
        AccidentReactionReportResponseDto result = accidentReactionReportService.getFullReport(reportId);

        // Then
        assertThat(result).isNotNull();
        
        // Task 1 검증
        assertThat(result.getReportId()).isEqualTo("123");
        assertThat(result.getReceivedAlertCount()).isEqualTo(10);
        assertThat(result.getAvgReactionSec()).isEqualTo(1.7);
        assertThat(result.getBrakeOrStopRatio()).isEqualTo(0.4);
        assertThat(result.getAvoidRatio()).isEqualTo(0.3);
        
        // Task 2 검증
        assertThat(result.getBenchmark()).isNotNull();
        assertThat(result.getBenchmark().getDeltaSec()).isEqualTo(-0.3); // 1.4 - 1.7 = -0.3 (더 느림)
        assertThat(result.getBenchmark().getChart().getValuesSec()).containsExactly(1.4, 1.7);
    }
}