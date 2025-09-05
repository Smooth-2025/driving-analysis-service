package com.smooth.driving_analysis_service.timeline.controller;

import com.smooth.driving_analysis_service.config.TestConfig;
import com.smooth.driving_analysis_service.timeline.dto.TimeLineResponseDto;
import com.smooth.driving_analysis_service.timeline.service.TimeLineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 타임라인 컨트롤러 단위테스트
 * 외부 서비스 의존성 없이 순수한 컨트롤러 로직만 테스트
 */
@WebMvcTest(TimeLineController.class)
@Import(TestConfig.class)
@DisplayName("타임라인 컨트롤러 단위테스트")
class TimeLineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TimeLineService timeLineService;

    @Test
    @DisplayName("전체 타임라인 조회 - 성공")
    void getTimeLine_Success() throws Exception {
        // Given
        TimeLineResponseDto mockResponse = createMockTimeLineResponse();
        given(timeLineService.getAllTimeLine(eq(1L), isNull(), eq(10))).willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/driving-analysis/timeline")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("타임라인 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.hasMore").value(true));
    }

    @Test
    @DisplayName("리포트 타임라인 조회 - 성공")
    void getReportsTimeLine_Success() throws Exception {
        // Given
        TimeLineResponseDto mockResponse = createMockReportResponse();
        given(timeLineService.getReportTimeLine(eq(1L), isNull(), eq(10))).willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/driving-analysis/timeline/reports")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("리포트 타임라인 조회가 완료되었습니다."));
    }

    @Test
    @DisplayName("주행 타임라인 조회 - 성공")
    void getDrivingTimeLine_Success() throws Exception {
        // Given
        TimeLineResponseDto mockResponse = createMockDrivingResponse();
        given(timeLineService.getDrivingTimeLine(eq(1L), isNull(), eq(10))).willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/driving-analysis/timeline/driving")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("주행 타임라인 조회가 완료되었습니다."));
    }

    @Test
    @DisplayName("커서 기반 페이징 - 빈 결과")
    void getTimeLineWithCursor_EmptyResult() throws Exception {
        // Given
        TimeLineResponseDto emptyResponse = TimeLineResponseDto.builder()
                .items(Collections.emptyList())
                .nextCursor(null)
                .hasMore(false)
                .build();
        given(timeLineService.getAllTimeLine(eq(1L), eq("2025-01-01T10:00:00"), eq(5))).willReturn(emptyResponse);

        // When & Then
        mockMvc.perform(get("/api/driving-analysis/timeline")
                        .param("cursor", "2025-01-01T10:00:00")
                        .param("limit", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isEmpty())
                .andExpect(jsonPath("$.data.hasMore").value(false));
    }

    @Test
    @DisplayName("기본 파라미터로 조회 - 성공")
    void getTimeLineWithDefaultParams_Success() throws Exception {
        // Given
        TimeLineResponseDto mockResponse = createMockTimeLineResponse();
        given(timeLineService.getAllTimeLine(eq(1L), isNull(), eq(10))).willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/driving-analysis/timeline")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // Mock 데이터 생성 헬퍼 메서드들
    private TimeLineResponseDto createMockTimeLineResponse() {
        return TimeLineResponseDto.builder()
                .items(Arrays.asList(
                        createMockDrivingItem(),
                        createMockReportItem()
                ))
                .nextCursor("2025-09-05T14:00:00")
                .hasMore(true)
                .build();
    }

    private TimeLineResponseDto createMockDrivingResponse() {
        return TimeLineResponseDto.builder()
                .items(Arrays.asList(createMockDrivingItem()))
                .nextCursor(null)
                .hasMore(false)
                .build();
    }

    private TimeLineResponseDto createMockReportResponse() {
        return TimeLineResponseDto.builder()
                .items(Arrays.asList(createMockReportItem()))
                .nextCursor(null)
                .hasMore(false)
                .build();
    }

    private TimeLineResponseDto.TimeLineItem createMockDrivingItem() {
        return TimeLineResponseDto.TimeLineItem.builder()
                .id("drive_1")
                .type("DRIVING")
                .createdAt(LocalDateTime.now())
                .data(new MockDrivingData())
                .build();
    }

    private TimeLineResponseDto.TimeLineItem createMockReportItem() {
        return TimeLineResponseDto.TimeLineItem.builder()
                .id("report_1")
                .type("REPORT")
                .createdAt(LocalDateTime.now())
                .data(new MockReportData())
                .build();
    }

    // Mock 데이터 클래스들
    private static class MockDrivingData {
        public final Long id = 1L;
        public final String status = "COMPLETED";
        public final Double totalDistance = 25.7;
        public final Integer drivingMinutes = 60;
    }

    private static class MockReportData {
        public final Long id = 1L;
        public final Boolean isRead = false;
        public final String status = "COMPLETED";
    }
}