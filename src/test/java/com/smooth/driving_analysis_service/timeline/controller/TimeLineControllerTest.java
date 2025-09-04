package com.smooth.driving_analysis_service.timeline.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smooth.driving_analysis_service.timeline.dto.DrivingRecordResponseDto;
import com.smooth.driving_analysis_service.timeline.dto.ReportSummaryResponseDto;
import com.smooth.driving_analysis_service.timeline.dto.TimeLineResponseDto;
import com.smooth.driving_analysis_service.timeline.service.TimeLineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TimeLineController.class)
class TimeLineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TimeLineService timeLineService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("전체 타임라인 조회 API 테스트")
    void getTimeLine() throws Exception {
        // given
        LocalDateTime now = LocalDateTime.now();
        
        TimeLineResponseDto.TimeLineItem drivingItem = TimeLineResponseDto.TimeLineItem.builder()
                .id("drive_1")
                .type("DRIVING")
                .createdAt(now.minusHours(1))
                .data(DrivingRecordResponseDto.builder()
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
                        .status("COMPLETED")
                        .build())
                .build();

        TimeLineResponseDto.TimeLineItem reportItem = TimeLineResponseDto.TimeLineItem.builder()
                .id("report_1")
                .type("REPORT")
                .createdAt(now.minusHours(2))
                .data(ReportSummaryResponseDto.builder()
                        .id(1L)
                        .isRead(false)
                        .status("COMPLETED")
                        .build())
                .build();

        TimeLineResponseDto response = TimeLineResponseDto.builder()
                .items(Arrays.asList(drivingItem, reportItem))
                .nextCursor(now.minusHours(2).toString())
                .hasMore(true)
                .build();

        when(timeLineService.getAllTimeLine(eq(1L), isNull(), eq(10)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/driving-analysis/timeline")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("타임라인 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andExpect(jsonPath("$.data.items[0].id").value("drive_1"))
                .andExpect(jsonPath("$.data.items[0].type").value("DRIVING"))
                .andExpect(jsonPath("$.data.items[1].id").value("report_1"))
                .andExpect(jsonPath("$.data.items[1].type").value("REPORT"))
                .andExpect(jsonPath("$.data.hasMore").value(true))
                .andExpect(jsonPath("$.data.nextCursor").exists());
    }

    @Test
    @DisplayName("리포트 타임라인 조회 API 테스트")
    void getReportsTimeLine() throws Exception {
        // given
        LocalDateTime now = LocalDateTime.now();
        
        TimeLineResponseDto.TimeLineItem reportItem = TimeLineResponseDto.TimeLineItem.builder()
                .id("report_1")
                .type("REPORT")
                .createdAt(now.minusHours(1))
                .data(ReportSummaryResponseDto.builder()
                        .id(1L)
                        .isRead(false)
                        .status("COMPLETED")
                        .build())
                .build();

        TimeLineResponseDto response = TimeLineResponseDto.builder()
                .items(Arrays.asList(reportItem))
                .nextCursor(null)
                .hasMore(false)
                .build();

        when(timeLineService.getReportTimeLine(eq(1L), isNull(), eq(10)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/driving-analysis/timeline/reports")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("리포트 타임라인 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value("report_1"))
                .andExpect(jsonPath("$.data.items[0].type").value("REPORT"))
                .andExpect(jsonPath("$.data.hasMore").value(false));
    }

    @Test
    @DisplayName("주행 타임라인 조회 API 테스트")
    void getDrivingTimeLine() throws Exception {
        // given
        LocalDateTime now = LocalDateTime.now();
        
        TimeLineResponseDto.TimeLineItem drivingItem = TimeLineResponseDto.TimeLineItem.builder()
                .id("drive_1")
                .type("DRIVING")
                .createdAt(now.minusHours(1))
                .data(DrivingRecordResponseDto.builder()
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
                        .build())
                .build();

        TimeLineResponseDto response = TimeLineResponseDto.builder()
                .items(Arrays.asList(drivingItem))
                .nextCursor(null)
                .hasMore(false)
                .build();

        when(timeLineService.getDrivingTimeLine(eq(1L), isNull(), eq(10)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/driving-analysis/timeline/driving")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("주행 타임라인 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value("drive_1"))
                .andExpect(jsonPath("$.data.items[0].type").value("DRIVING"));
    }

    @Test
    @DisplayName("커서 기반 페이징 테스트")
    void getTimeLine_WithCursor() throws Exception {
        // given
        String cursor = "2025-01-01T10:00:00";
        TimeLineResponseDto response = TimeLineResponseDto.builder()
                .items(Arrays.asList())
                .nextCursor(null)
                .hasMore(false)
                .build();

        when(timeLineService.getAllTimeLine(eq(1L), eq(cursor), eq(5)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/driving-analysis/timeline")
                        .param("cursor", cursor)
                        .param("limit", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.hasMore").value(false));
    }

    @Test
    @DisplayName("기본 limit 값 테스트")
    void getTimeLine_DefaultLimit() throws Exception {
        // given
        TimeLineResponseDto response = TimeLineResponseDto.builder()
                .items(Arrays.asList())
                .nextCursor(null)
                .hasMore(false)
                .build();

        when(timeLineService.getAllTimeLine(eq(1L), isNull(), eq(10))) // 기본값 10
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/driving-analysis/timeline")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }
}