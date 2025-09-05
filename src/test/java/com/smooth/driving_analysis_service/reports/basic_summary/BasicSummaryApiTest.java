package com.smooth.driving_analysis_service.reports.basic_summary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smooth.driving_analysis_service.reports.basic_summary.entity.BasicSummary;
import com.smooth.driving_analysis_service.reports.basic_summary.repository.BasicSummaryRepository;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.reports.basic_summary.controller.BasicSummaryController;
import com.smooth.driving_analysis_service.reports.basic_summary.dto.BasicSummaryResponse;
import com.smooth.driving_analysis_service.reports.basic_summary.service.BasicSummaryService;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BasicSummaryApiTest {

    @Mock
    private BasicSummaryService basicSummaryService;

    @InjectMocks
    private BasicSummaryController basicSummaryController;

    private Long testReportId = 1L;

    @Test
    @DisplayName("리포트 상단 요약 조회 - 성공")
    void getBasicSummary_Success() {
        // given
        BasicSummaryResponse mockResponse = BasicSummaryResponse.builder()
                .reportId("u12345_r1_20250906")
                .totalDistanceKm(26.6)
                .averageDurationSec(38.25)
                .averageDistanceKm(1.77)
                .averageSpeedKmh(42.3)
                .averageCruiseRatio(0.684)
                .periodStart(LocalDate.of(2025, 8, 1))
                .periodEnd(LocalDate.of(2025, 8, 28))
                .build();

        when(basicSummaryService.getBasicSummary(testReportId)).thenReturn(mockResponse);

        // when
        ApiResponse<BasicSummaryResponse> response = basicSummaryController.getBasicSummary(testReportId);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("리포트 상단 요약 조회 완료");
        assertThat(response.getData().getTotalDistanceKm()).isEqualTo(26.6);
        assertThat(response.getData().getAverageDurationSec()).isEqualTo(38.25);
        assertThat(response.getData().getAverageDistanceKm()).isEqualTo(1.77);
        assertThat(response.getData().getAverageSpeedKmh()).isEqualTo(42.3);
        assertThat(response.getData().getAverageCruiseRatio()).isEqualTo(0.684);
        assertThat(response.getData().getPeriodStart()).isEqualTo(LocalDate.of(2025, 8, 1));
        assertThat(response.getData().getPeriodEnd()).isEqualTo(LocalDate.of(2025, 8, 28));
    }

    @Test
    @DisplayName("리포트 상단 요약 조회 - INTERIM 스냅샷 조회")
    void getBasicSummary_InterimSnapshot() {
        // given
        BasicSummaryResponse mockResponse = BasicSummaryResponse.builder()
                .reportId("u12345_r1_20250906")
                .totalDistanceKm(15.5)
                .averageDurationSec(30.0)
                .averageDistanceKm(1.5)
                .averageSpeedKmh(40.0)
                .averageCruiseRatio(0.65)
                .periodStart(LocalDate.of(2025, 8, 1))
                .periodEnd(LocalDate.of(2025, 8, 15))
                .build();

        when(basicSummaryService.getBasicSummary(testReportId)).thenReturn(mockResponse);

        // when
        ApiResponse<BasicSummaryResponse> response = basicSummaryController.getBasicSummary(testReportId);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getTotalDistanceKm()).isEqualTo(15.5);
        assertThat(response.getData().getAverageDurationSec()).isEqualTo(30.0);
    }

    @Test
    @DisplayName("리포트 상단 요약 조회 - 존재하지 않는 리포트")
    void getBasicSummary_NotFound() {
        // given
        Long nonExistentReportId = 99999L;
        when(basicSummaryService.getBasicSummary(nonExistentReportId)).thenReturn(null);

        // when
        ApiResponse<BasicSummaryResponse> response = basicSummaryController.getBasicSummary(nonExistentReportId);

        // then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getCode()).isEqualTo(404);
        assertThat(response.getMessage()).isEqualTo("리포트 요약을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("리포트 ID 형식 검증")
    void getBasicSummary_ReportIdFormat() {
        // given
        BasicSummaryResponse mockResponse = BasicSummaryResponse.builder()
                .reportId("u12345_r1_20250906")
                .totalDistanceKm(26.6)
                .averageDurationSec(38.25)
                .averageDistanceKm(1.77)
                .averageSpeedKmh(42.3)
                .averageCruiseRatio(0.684)
                .periodStart(LocalDate.of(2025, 8, 1))
                .periodEnd(LocalDate.of(2025, 8, 28))
                .build();

        when(basicSummaryService.getBasicSummary(testReportId)).thenReturn(mockResponse);

        // when
        ApiResponse<BasicSummaryResponse> response = basicSummaryController.getBasicSummary(testReportId);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getReportId()).matches("u\\d+_r\\d+_\\d{8}");
    }
}