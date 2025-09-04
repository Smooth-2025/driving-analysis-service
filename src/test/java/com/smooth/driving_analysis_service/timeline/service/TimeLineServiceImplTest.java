package com.smooth.driving_analysis_service.timeline.service;

import com.smooth.driving_analysis_service.driving.entity.DrivingRecord;
import com.smooth.driving_analysis_service.driving.entity.SummaryStatus;
import com.smooth.driving_analysis_service.driving.repository.DrivingRecordRepository;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import com.smooth.driving_analysis_service.timeline.dto.TimeLineResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeLineServiceImplTest {

    @Mock
    private DrivingRecordRepository drivingRecordRepository;

    @Mock
    private MilestoneReportRepository milestoneReportRepository;

    @InjectMocks
    private TimeLineServiceImpl timeLineService;

    private DrivingRecord drivingRecord1;
    private DrivingRecord drivingRecord2;
    private MilestoneReport milestoneReport1;
    private MilestoneReport milestoneReport2;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        drivingRecord1 = DrivingRecord.builder()
                .id(1L)
                .userId(1L)
                .drivingId("driving_1")
                .startTime(now.minusHours(2))
                .endTime(now.minusHours(1))
                .totalDistance(25.7)
                .avgSpeed(33.7)
                .cruiseRatio(0.78)
                .laneChangeCount(4)
                .hardBrakeCount(1)
                .rapidAccelCount(2)
                .sharpTurnCount(3)
                .status(SummaryStatus.COMPLETED)
                .createdAt(now.minusHours(1))
                .build();

        drivingRecord2 = DrivingRecord.builder()
                .id(2L)
                .userId(1L)
                .drivingId("driving_2")
                .startTime(now.minusHours(4))
                .endTime(now.minusHours(3))
                .totalDistance(18.3)
                .avgSpeed(28.5)
                .cruiseRatio(0.72)
                .laneChangeCount(6)
                .hardBrakeCount(0)
                .rapidAccelCount(1)
                .sharpTurnCount(2)
                .status(SummaryStatus.COMPLETED)
                .createdAt(now.minusHours(3))
                .build();

        milestoneReport1 = MilestoneReport.builder()
                .id(1L)
                .userId(1L)
                .cycleNo(1)
                .numberOfDriving(15)
                .status(MilestoneReport.Status.COMPLETED)
                .read(false)
                .createdAt(now.minusHours(2))
                .build();

        milestoneReport2 = MilestoneReport.builder()
                .id(2L)
                .userId(1L)
                .cycleNo(2)
                .numberOfDriving(10)
                .status(MilestoneReport.Status.PROCESSING)
                .read(false)
                .createdAt(now.minusHours(4))
                .build();
    }

    @Test
    @DisplayName("주행 타임라인 조회 - 첫 번째 페이지")
    void getDrivingTimeLine_FirstPage() {
        // given
        Long userId = 1L;
        int limit = 10;
        Page<DrivingRecord> page = new PageImpl<>(Arrays.asList(drivingRecord1, drivingRecord2));
        
        when(drivingRecordRepository.findByUserIdOrderByEndTimeDesc(eq(userId), any(PageRequest.class)))
                .thenReturn(page);

        // when
        TimeLineResponseDto result = timeLineService.getDrivingTimeLine(userId, null, limit);

        // then
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().get(0).getId()).isEqualTo("drive_1");
        assertThat(result.getItems().get(0).getType()).isEqualTo("DRIVING");
        assertThat(result.getItems().get(1).getId()).isEqualTo("drive_2");
        assertThat(result.isHasMore()).isFalse();
    }

    @Test
    @DisplayName("주행 타임라인 조회 - 커서 기반 페이징")
    void getDrivingTimeLine_WithCursor() {
        // given
        Long userId = 1L;
        String cursor = "2025-01-01T10:00:00";
        int limit = 10;
        LocalDateTime cursorTime = LocalDateTime.parse(cursor);
        Page<DrivingRecord> page = new PageImpl<>(Arrays.asList(drivingRecord2));
        
        when(drivingRecordRepository.findByUserIdAndEndTimeBeforeOrderByEndTimeDesc(
                eq(userId), eq(cursorTime), any(PageRequest.class)))
                .thenReturn(page);

        // when
        TimeLineResponseDto result = timeLineService.getDrivingTimeLine(userId, cursor, limit);

        // then
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getId()).isEqualTo("drive_2");
        assertThat(result.isHasMore()).isFalse();
    }

    @Test
    @DisplayName("리포트 타임라인 조회 - PROCESSING/COMPLETED만 조회")
    void getReportTimeLine() {
        // given
        Long userId = 1L;
        int limit = 10;
        List<MilestoneReport.Status> statuses = Arrays.asList(
                MilestoneReport.Status.PROCESSING, 
                MilestoneReport.Status.COMPLETED
        );
        Page<MilestoneReport> page = new PageImpl<>(Arrays.asList(milestoneReport1, milestoneReport2));
        
        when(milestoneReportRepository.findByUserIdAndStatusInOrderByCreatedAtDesc(
                eq(userId), eq(statuses), any(PageRequest.class)))
                .thenReturn(page);

        // when
        TimeLineResponseDto result = timeLineService.getReportTimeLine(userId, null, limit);

        // then
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().get(0).getId()).isEqualTo("report_1");
        assertThat(result.getItems().get(0).getType()).isEqualTo("REPORT");
        assertThat(result.getItems().get(1).getId()).isEqualTo("report_2");
    }

    @Test
    @DisplayName("전체 타임라인 조회 - 주행과 리포트 합쳐서 시간순 정렬")
    void getAllTimeLine() {
        // given
        Long userId = 1L;
        int limit = 10;
        
        Page<DrivingRecord> drivingPage = new PageImpl<>(Arrays.asList(drivingRecord1, drivingRecord2));
        Page<MilestoneReport> reportPage = new PageImpl<>(Arrays.asList(milestoneReport1, milestoneReport2));
        
        when(drivingRecordRepository.findByUserIdOrderByEndTimeDesc(eq(userId), any(PageRequest.class)))
                .thenReturn(drivingPage);
        when(milestoneReportRepository.findByUserIdAndStatusInOrderByCreatedAtDesc(
                eq(userId), anyList(), any(PageRequest.class)))
                .thenReturn(reportPage);

        // when
        TimeLineResponseDto result = timeLineService.getAllTimeLine(userId, null, limit);

        // then
        assertThat(result.getItems()).hasSize(4);
        
        // 주행과 리포트가 모두 포함되어야 함
        long drivingCount = result.getItems().stream()
                .filter(item -> "DRIVING".equals(item.getType()))
                .count();
        long reportCount = result.getItems().stream()
                .filter(item -> "REPORT".equals(item.getType()))
                .count();
        
        assertThat(drivingCount).isEqualTo(2);
        assertThat(reportCount).isEqualTo(2);
    }

    @Test
    @DisplayName("hasMore 플래그 테스트 - limit보다 많은 데이터가 있을 때")
    void hasMore_WhenMoreDataExists() {
        // given
        Long userId = 1L;
        int limit = 1;
        
        // limit + 1 개의 데이터를 반환하도록 설정
        Page<DrivingRecord> page = new PageImpl<>(Arrays.asList(drivingRecord1, drivingRecord2));
        
        when(drivingRecordRepository.findByUserIdOrderByEndTimeDesc(eq(userId), any(PageRequest.class)))
                .thenReturn(page);

        // when
        TimeLineResponseDto result = timeLineService.getDrivingTimeLine(userId, null, limit);

        // then
        assertThat(result.getItems()).hasSize(1); // limit만큼만 반환
        assertThat(result.isHasMore()).isTrue(); // 더 많은 데이터가 있음을 표시
        assertThat(result.getNextCursor()).isNotNull();
    }

    @Test
    @DisplayName("nextCursor 생성 테스트")
    void nextCursor_Generation() {
        // given
        Long userId = 1L;
        int limit = 10;
        Page<DrivingRecord> page = new PageImpl<>(Arrays.asList(drivingRecord1));
        
        when(drivingRecordRepository.findByUserIdOrderByEndTimeDesc(eq(userId), any(PageRequest.class)))
                .thenReturn(page);

        // when
        TimeLineResponseDto result = timeLineService.getDrivingTimeLine(userId, null, limit);

        // then
        assertThat(result.getNextCursor()).isNotNull();
        assertThat(result.getNextCursor()).contains("T"); // LocalDateTime 형식
    }
}