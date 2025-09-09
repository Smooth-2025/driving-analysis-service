package com.smooth.driving_analysis_service.reports.basic_summary;

import com.smooth.driving_analysis_service.reports.basic_summary.dto.BasicSummaryResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BasicSummaryResponse DTO 단위테스트")
class BasicSummaryDtoTest {

    @Test
    @DisplayName("DTO 빌더 패턴 검증")
    void builderPattern() {
        // given & when
        BasicSummaryResponseDto response = BasicSummaryResponseDto.builder()
                .reportId("u12345_r1_20250906")
                .totalDistanceKm(100.5)
                .averageSpeedKmh(60.0)
                .averageCruiseRatio(0.8)
                .periodStart(LocalDate.of(2025, 8, 1))
                .periodEnd(LocalDate.of(2025, 8, 31))
                .build();

        // then
        assertThat(response.getReportId()).isEqualTo("u12345_r1_20250906");
        assertThat(response.getTotalDistanceKm()).isEqualTo(100.5);
        assertThat(response.getAverageSpeedKmh()).isEqualTo(60.0);
        assertThat(response.getAverageCruiseRatio()).isEqualTo(0.8);
        assertThat(response.getPeriodStart()).isEqualTo(LocalDate.of(2025, 8, 1));
        assertThat(response.getPeriodEnd()).isEqualTo(LocalDate.of(2025, 8, 31));
    }

    @Test
    @DisplayName("DTO null 값 처리")
    void nullValues() {
        // given & when
        BasicSummaryResponseDto response = BasicSummaryResponseDto.builder()
                .reportId("test")
                .build();

        // then
        assertThat(response.getReportId()).isEqualTo("test");
        assertThat(response.getTotalDistanceKm()).isNull();
        assertThat(response.getAverageSpeedKmh()).isNull();
    }

    @Test
    @DisplayName("DTO 기본값 검증")
    void defaultValues() {
        // given & when
        BasicSummaryResponseDto response = BasicSummaryResponseDto.builder()
                .reportId("test")
                .totalDistanceKm(0.0)
                .averageSpeedKmh(0.0)
                .build();

        // then
        assertThat(response.getTotalDistanceKm()).isEqualTo(0.0);
        assertThat(response.getAverageSpeedKmh()).isEqualTo(0.0);
    }
}