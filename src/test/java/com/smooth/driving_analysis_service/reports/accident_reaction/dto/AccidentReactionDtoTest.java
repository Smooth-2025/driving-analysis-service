package com.smooth.driving_analysis_service.reports.accident_reaction.dto;

import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.AccidentReactionBasicMetricsDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.AccidentReactionBenchmarkDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.AccidentReactionReportResponseDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.request.AccidentReactionRenderedRequestDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccidentReactionDtoTest {

    @Test
    void testAccidentReactionBasicMetricsDto_Builder() {
        // When
        AccidentReactionBasicMetricsDto dto = AccidentReactionBasicMetricsDto.builder()
                .receivedAlertCount(15)
                .avgReactionSec(1.8)
                .brakeOrStopRatio(0.4)
                .avoidRatio(0.15)
                .build();

        // Then
        assertNotNull(dto);
        assertEquals(15, dto.getReceivedAlertCount());
        assertEquals(1.8, dto.getAvgReactionSec());
        assertEquals(0.4, dto.getBrakeOrStopRatio());
        assertEquals(0.15, dto.getAvoidRatio());
    }

    @Test
    void testAccidentReactionBenchmarkDto_Builder() {
        // Given
        AccidentReactionBenchmarkDto.ChartDto chart = AccidentReactionBenchmarkDto.ChartDto.builder()
                .labels(new String[]{"일반 운전자", "내 주행"})
                .valuesSec(new Double[]{1.5, 1.2})
                .build();

        // When
        AccidentReactionBenchmarkDto dto = AccidentReactionBenchmarkDto.builder()
                .deltaSec(0.3)
                .chart(chart)
                .build();

        // Then
        assertNotNull(dto);
        assertEquals(0.3, dto.getDeltaSec());
        assertNotNull(dto.getChart());
        assertArrayEquals(new String[]{"일반 운전자", "내 주행"}, dto.getChart().getLabels());
        assertArrayEquals(new Double[]{1.5, 1.2}, dto.getChart().getValuesSec());
    }

    @Test
    void testAccidentReactionReportResponseDto_Builder() {
        // Given
        AccidentReactionBenchmarkDto.ChartDto chart = AccidentReactionBenchmarkDto.ChartDto.builder()
                .labels(new String[]{"일반 운전자", "내 주행"})
                .valuesSec(new Double[]{1.6, 1.9})
                .build();
        
        AccidentReactionBenchmarkDto benchmark = AccidentReactionBenchmarkDto.builder()
                .deltaSec(-0.3)
                .chart(chart)
                .build();

        // When
        AccidentReactionReportResponseDto dto = AccidentReactionReportResponseDto.builder()
                .reportId("report_123")
                .receivedAlertCount(20)
                .avgReactionSec(1.9)
                .brakeOrStopRatio(0.45)
                .avoidRatio(0.2)
                .benchmark(benchmark)
                .build();

        // Then
        assertNotNull(dto);
        assertEquals("report_123", dto.getReportId());
        assertEquals(20, dto.getReceivedAlertCount());
        assertEquals(1.9, dto.getAvgReactionSec());
        assertEquals(0.45, dto.getBrakeOrStopRatio());
        assertEquals(0.2, dto.getAvoidRatio());
        assertNotNull(dto.getBenchmark());
        assertEquals(-0.3, dto.getBenchmark().getDeltaSec());
    }

    @Test
    void testAccidentReactionRenderedRequestDto_Builder() {
        // When
        AccidentReactionRenderedRequestDto dto = AccidentReactionRenderedRequestDto.builder()
                .renderedAtMs(1724823001230L)
                .type("accident-nearby")
                .build();

        // Then
        assertNotNull(dto);
        assertEquals(1724823001230L, dto.getRenderedAtMs());
        assertEquals("accident-nearby", dto.getType());
    }

    @Test
    void testAccidentReactionRenderedRequestDto_SettersGetters() {
        // Given
        AccidentReactionRenderedRequestDto dto = new AccidentReactionRenderedRequestDto();

        // When
        dto.setRenderedAtMs(1724823001500L);
        dto.setType("obstacle");

        // Then
        assertEquals(1724823001500L, dto.getRenderedAtMs());
        assertEquals("obstacle", dto.getType());
    }

    @Test
    void testChartDto_EmptyArrays() {
        // When
        AccidentReactionBenchmarkDto.ChartDto chart = AccidentReactionBenchmarkDto.ChartDto.builder()
                .labels(new String[]{})
                .valuesSec(new Double[]{})
                .build();

        // Then
        assertNotNull(chart);
        assertNotNull(chart.getLabels());
        assertNotNull(chart.getValuesSec());
        assertEquals(0, chart.getLabels().length);
        assertEquals(0, chart.getValuesSec().length);
    }

    @Test
    void testAccidentReactionBasicMetricsDto_NullValues() {
        // When
        AccidentReactionBasicMetricsDto dto = AccidentReactionBasicMetricsDto.builder()
                .receivedAlertCount(null)
                .avgReactionSec(null)
                .brakeOrStopRatio(null)
                .avoidRatio(null)
                .build();

        // Then
        assertNotNull(dto);
        assertNull(dto.getReceivedAlertCount());
        assertNull(dto.getAvgReactionSec());
        assertNull(dto.getBrakeOrStopRatio());
        assertNull(dto.getAvoidRatio());
    }
}