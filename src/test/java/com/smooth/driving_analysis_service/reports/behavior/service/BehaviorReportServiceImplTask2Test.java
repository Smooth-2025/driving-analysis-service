package com.smooth.driving_analysis_service.reports.behavior.service;

import com.smooth.driving_analysis_service.reports.behavior.dto.projection.EventPatternProjection;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.BehaviorAnalysisResponseDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.DrivingPatternDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.TotalCountsDto;
import com.smooth.driving_analysis_service.reports.behavior.repository.BehaviorPatternRepository;
import com.smooth.driving_analysis_service.reports.behavior.repository.BehaviorTotalCountsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BehaviorReportService Task 2 테스트")
class BehaviorReportServiceImplTask2Test {

    @Mock
    private BehaviorTotalCountsRepository totalCountsRepository;
    
    @Mock
    private BehaviorPatternRepository behaviorPatternRepository;
    
    @Mock
    private BehaviorPatternAnalyzer patternAnalyzer;

    @InjectMocks
    private BehaviorReportServiceImpl behaviorReportService;

    @Test
    @DisplayName("Task 2: totalCounts + drivingPattern 정상 조회")
    void getBehaviorAnalysis_WithDrivingPattern_Success() {
        // given
        String reportId = "u1_r3_20250901";
        TotalCountsDto mockTotalCounts = TotalCountsDto.of(38, 42, 17);
        List<EventPatternProjection> mockEventPatterns = createMockEventPatterns();
        DrivingPatternDto mockDrivingPattern = createMockDrivingPattern();
        
        when(totalCountsRepository.findTotalCountsByReportId(3L))
                .thenReturn(mockTotalCounts);
        when(behaviorPatternRepository.findEventPatternsByReportId(3L))
                .thenReturn(mockEventPatterns);
        when(patternAnalyzer.analyzeDrivingPattern(any()))
                .thenReturn(mockDrivingPattern);

        // when
        BehaviorAnalysisResponseDto result = behaviorReportService.getBehaviorAnalysis(reportId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getReportId()).isEqualTo(reportId);
        
        // Task 1: totalCounts 검증
        assertThat(result.getTotalCounts()).isNotNull();
        assertThat(result.getTotalCounts().getHardBrake()).isEqualTo(38);
        assertThat(result.getTotalCounts().getRapidAccel()).isEqualTo(42);
        assertThat(result.getTotalCounts().getLaneChange()).isEqualTo(17);
        assertThat(result.getTotalCounts().getTotal()).isEqualTo(97);
        
        // Task 2: drivingPattern 검증
        assertThat(result.getDrivingPattern()).isNotNull();
        assertThat(result.getDrivingPattern().getWeekday()).isEqualTo("금요일");
        assertThat(result.getDrivingPattern().getTimeslot()).isEqualTo("저녁");
        assertThat(result.getDrivingPattern().getChart()).hasSize(7);
        assertThat(result.getDrivingPattern().getComment()).isNotNull();
    }

    @Test
    @DisplayName("Task 2: 패턴 데이터 없을 때 기본값 반환")
    void getBehaviorAnalysis_NoPatternData() {
        // given
        String reportId = "u1_r999_20250901";
        TotalCountsDto mockTotalCounts = TotalCountsDto.of(5, 3, 2);
        DrivingPatternDto emptyPattern = createEmptyDrivingPattern();
        
        when(totalCountsRepository.findTotalCountsByReportId(anyLong()))
                .thenReturn(mockTotalCounts);
        when(behaviorPatternRepository.findEventPatternsByReportId(anyLong()))
                .thenReturn(new ArrayList<>());
        when(patternAnalyzer.analyzeDrivingPattern(any()))
                .thenReturn(emptyPattern);

        // when
        BehaviorAnalysisResponseDto result = behaviorReportService.getBehaviorAnalysis(reportId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getDrivingPattern()).isNotNull();
        assertThat(result.getDrivingPattern().getComment()).contains("충분한 주행 데이터가 없어");
    }

    @Test
    @DisplayName("Task 2: 예외 발생시 기본 응답 반환")
    void getBehaviorAnalysis_ExceptionHandling() {
        // given
        String reportId = "invalid_format";
        
        when(totalCountsRepository.findTotalCountsByReportId(anyLong()))
                .thenThrow(new RuntimeException("Database error"));

        // when
        BehaviorAnalysisResponseDto result = behaviorReportService.getBehaviorAnalysis(reportId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getReportId()).isEqualTo(reportId);
        assertThat(result.getTotalCounts().getTotal()).isEqualTo(0);
        assertThat(result.getDrivingPattern()).isNotNull();
        assertThat(result.getDrivingPattern().getComment()).contains("오류가 발생했습니다");
    }

    private List<EventPatternProjection> createMockEventPatterns() {
        return List.of(
                createEventPattern(5, "EVENING", "hard_brake", 8),
                createEventPattern(5, "COMMUTE_FROM_WORK", "rapid_accel", 6)
        );
    }

    private EventPatternProjection createEventPattern(int weekday, String timeSlot, String eventType, int count) {
        return new EventPatternProjection() {
            @Override
            public Integer getWeekday() { return weekday; }
            @Override
            public String getTimeSlot() { return timeSlot; }
            @Override
            public String getEventType() { return eventType; }
            @Override
            public Integer getEventCount() { return count; }
        };
    }

    private DrivingPatternDto createMockDrivingPattern() {
        List<DrivingPatternDto.WeeklyChartDto> charts = new ArrayList<>();
        String[] weekdays = {"월", "화", "수", "목", "금", "토", "일"};
        
        for (String weekday : weekdays) {
            charts.add(DrivingPatternDto.WeeklyChartDto.builder()
                    .weekday(weekday)
                    .actions(DrivingPatternDto.ActionsDto.builder()
                            .hardBrake(DrivingPatternDto.ActionTimeSlotDto.builder()
                                    .timeSlot("퇴근").count(5).build())
                            .rapidAccel(DrivingPatternDto.ActionTimeSlotDto.builder()
                                    .timeSlot("출근").count(3).build())
                            .laneChange(DrivingPatternDto.ActionTimeSlotDto.builder()
                                    .timeSlot("낮").count(2).build())
                            .build())
                    .build());
        }

        return DrivingPatternDto.builder()
                .weekday("금요일")
                .timeslot("저녁")
                .chart(charts)
                .comment("평일 저녁에는 급제동과 급가속이 늘어나는 패턴이 보여요!")
                .build();
    }

    private DrivingPatternDto createEmptyDrivingPattern() {
        List<DrivingPatternDto.WeeklyChartDto> emptyCharts = new ArrayList<>();
        String[] weekdays = {"월", "화", "수", "목", "금", "토", "일"};
        
        for (String weekday : weekdays) {
            emptyCharts.add(DrivingPatternDto.WeeklyChartDto.builder()
                    .weekday(weekday)
                    .actions(DrivingPatternDto.ActionsDto.builder()
                            .hardBrake(null).rapidAccel(null).laneChange(null)
                            .build())
                    .build());
        }

        return DrivingPatternDto.builder()
                .weekday("금요일")
                .timeslot("저녁")
                .chart(emptyCharts)
                .comment("아직 충분한 주행 데이터가 없어 패턴을 분석할 수 없습니다.")
                .build();
    }
}