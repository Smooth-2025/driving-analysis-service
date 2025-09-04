package com.smooth.driving_analysis_service.timeline.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TimeLineResponseDtoTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("TimeLineResponseDto JSON 직렬화 테스트")
    void jsonSerialization() throws Exception {
        // given
        LocalDateTime now = LocalDateTime.now();
        
        DrivingRecordResponseDto drivingData = DrivingRecordResponseDto.builder()
                .id(1L)
                .startTime(now.minusHours(2))
                .endTime(now.minusHours(1))
                .totalDistance(25.7)
                .avgSpeed(33.7)
                .cruiseRatio(78.0)
                .laneChangeCount(4)
                .hardBrakeCount(1)
                .rapidAccelCount(2)
                .sharpTurnCount(3)
                .drivingMinutes(60)
                .build();

        ReportSummaryResponseDto reportData = ReportSummaryResponseDto.builder()
                .id(1L)
                .isRead(false)
                .status("COMPLETED")
                .build();

        TimeLineResponseDto.TimeLineItem drivingItem = TimeLineResponseDto.TimeLineItem.builder()
                .id("drive_1")
                .type("DRIVING")
                .createdAt(now)
                .data(drivingData)
                .build();

        TimeLineResponseDto.TimeLineItem reportItem = TimeLineResponseDto.TimeLineItem.builder()
                .id("report_1")
                .type("REPORT")
                .createdAt(now.minusHours(1))
                .data(reportData)
                .build();

        TimeLineResponseDto response = TimeLineResponseDto.builder()
                .items(Arrays.asList(drivingItem, reportItem))
                .nextCursor(now.minusHours(1).toString())
                .hasMore(true)
                .build();

        // when
        String json = objectMapper.writeValueAsString(response);

        // then
        assertThat(json).contains("\"id\":\"drive_1\"");
        assertThat(json).contains("\"type\":\"DRIVING\"");
        assertThat(json).contains("\"id\":\"report_1\"");
        assertThat(json).contains("\"type\":\"REPORT\"");
        assertThat(json).contains("\"hasMore\":true");
        assertThat(json).contains("\"nextCursor\"");
    }

    @Test
    @DisplayName("주행 데이터 응답 구조 검증")
    void drivingDataStructure() {
        // given
        LocalDateTime now = LocalDateTime.now();
        
        DrivingRecordResponseDto drivingData = DrivingRecordResponseDto.builder()
                .id(123L)
                .startTime(now.minusMinutes(76))
                .endTime(now)
                .totalDistance(25.7)
                .avgSpeed(33.7)
                .cruiseRatio(78.0)
                .laneChangeCount(4)
                .hardBrakeCount(1)
                .rapidAccelCount(2)
                .sharpTurnCount(3)
                .drivingMinutes(76)
                .build();

        TimeLineResponseDto.TimeLineItem item = TimeLineResponseDto.TimeLineItem.builder()
                .id("drive_123")
                .type("DRIVING")
                .createdAt(now)
                .data(drivingData)
                .build();

        // then
        assertThat(item.getId()).isEqualTo("drive_123");
        assertThat(item.getType()).isEqualTo("DRIVING");
        assertThat(item.getData()).isInstanceOf(DrivingRecordResponseDto.class);
        
        DrivingRecordResponseDto data = (DrivingRecordResponseDto) item.getData();
        assertThat(data.getId()).isEqualTo(123L);
        assertThat(data.getTotalDistance()).isEqualTo(25.7);
        assertThat(data.getAvgSpeed()).isEqualTo(33.7);
        assertThat(data.getCruiseRatio()).isEqualTo(78.0);
        assertThat(data.getDrivingMinutes()).isEqualTo(76);
    }

    @Test
    @DisplayName("리포트 데이터 응답 구조 검증")
    void reportDataStructure() {
        // given
        LocalDateTime now = LocalDateTime.now();
        
        ReportSummaryResponseDto reportData = ReportSummaryResponseDto.builder()
                .id(456L)
                .isRead(false)
                .status("COMPLETED")
                .build();

        TimeLineResponseDto.TimeLineItem item = TimeLineResponseDto.TimeLineItem.builder()
                .id("report_456")
                .type("REPORT")
                .createdAt(now)
                .data(reportData)
                .build();

        // then
        assertThat(item.getId()).isEqualTo("report_456");
        assertThat(item.getType()).isEqualTo("REPORT");
        assertThat(item.getData()).isInstanceOf(ReportSummaryResponseDto.class);
        
        ReportSummaryResponseDto data = (ReportSummaryResponseDto) item.getData();
        assertThat(data.getId()).isEqualTo(456L);
        assertThat(data.getIsRead()).isFalse();
        assertThat(data.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("페이징 정보 검증")
    void pagingInfo() {
        // given
        TimeLineResponseDto response = TimeLineResponseDto.builder()
                .items(Arrays.asList())
                .nextCursor("2025-08-11T16:45:00")
                .hasMore(true)
                .build();

        // then
        assertThat(response.getNextCursor()).isEqualTo("2025-08-11T16:45:00");
        assertThat(response.isHasMore()).isTrue();
        assertThat(response.getItems()).isEmpty();
    }
}