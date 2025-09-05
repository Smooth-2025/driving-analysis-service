package com.smooth.driving_analysis_service.reports.behavior.service;

import com.smooth.driving_analysis_service.reports.behavior.dto.projection.EventPatternProjection;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.DrivingPatternDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("BehaviorPatternAnalyzer 테스트")
class BehaviorPatternAnalyzerTest {

    @InjectMocks
    private BehaviorPatternAnalyzer patternAnalyzer;

    @Test
    @DisplayName("정상적인 이벤트 패턴 분석")
    void analyzeDrivingPattern_Success() {
        // given
        List<EventPatternProjection> eventPatterns = createMockEventPatterns();

        // when
        DrivingPatternDto result = patternAnalyzer.analyzeDrivingPattern(eventPatterns);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getWeekday()).isNotNull();
        assertThat(result.getTimeslot()).isNotNull();
        assertThat(result.getChart()).hasSize(7); // 월~일
        assertThat(result.getComment()).isNotNull();

        // 각 요일별 데이터 확인
        result.getChart().forEach(weeklyChart -> {
            assertThat(weeklyChart.getWeekday()).isNotNull();
            assertThat(weeklyChart.getActions()).isNotNull();
        });
    }

    @Test
    @DisplayName("빈 데이터일 때 기본 패턴 반환")
    void analyzeDrivingPattern_EmptyData() {
        // given
        List<EventPatternProjection> emptyPatterns = new ArrayList<>();

        // when
        DrivingPatternDto result = patternAnalyzer.analyzeDrivingPattern(emptyPatterns);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getWeekday()).isEqualTo("금요일");
        assertThat(result.getTimeslot()).isEqualTo("저녁");
        assertThat(result.getChart()).hasSize(7);
        assertThat(result.getComment()).contains("충분한 주행 데이터가 없어");

        // 모든 요일의 actions가 null인지 확인
        result.getChart().forEach(weeklyChart -> {
            assertThat(weeklyChart.getActions().getHardBrake()).isNull();
            assertThat(weeklyChart.getActions().getRapidAccel()).isNull();
            assertThat(weeklyChart.getActions().getLaneChange()).isNull();
        });
    }

    @Test
    @DisplayName("특정 요일에만 데이터가 있는 경우")
    void analyzeDrivingPattern_PartialData() {
        // given
        List<EventPatternProjection> partialPatterns = List.of(
                createEventPattern(5, "EVENING", "hard_brake", 10), // 금요일 저녁 급제동
                createEventPattern(5, "COMMUTE_FROM_WORK", "rapid_accel", 5) // 금요일 퇴근 급가속
        );

        // when
        DrivingPatternDto result = patternAnalyzer.analyzeDrivingPattern(partialPatterns);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getWeekday()).isEqualTo("금");
        
        // 금요일 데이터 확인
        DrivingPatternDto.WeeklyChartDto fridayChart = result.getChart().get(4); // 금요일 (0-based index)
        assertThat(fridayChart.getWeekday()).isEqualTo("금");
        assertThat(fridayChart.getActions().getHardBrake()).isNotNull();
        assertThat(fridayChart.getActions().getHardBrake().getTimeSlot()).isEqualTo("저녁");
        assertThat(fridayChart.getActions().getHardBrake().getCount()).isEqualTo(10);
        
        // 다른 요일은 데이터 없음
        DrivingPatternDto.WeeklyChartDto mondayChart = result.getChart().get(0); // 월요일
        assertThat(mondayChart.getActions().getHardBrake()).isNull();
    }

    private List<EventPatternProjection> createMockEventPatterns() {
        return List.of(
                // 월요일
                createEventPattern(1, "COMMUTE_TO_WORK", "hard_brake", 3),
                createEventPattern(1, "DAYTIME", "rapid_accel", 2),
                createEventPattern(1, "EVENING", "lane_change", 4),
                
                // 화요일
                createEventPattern(2, "COMMUTE_FROM_WORK", "hard_brake", 5),
                createEventPattern(2, "EVENING", "rapid_accel", 3),
                createEventPattern(2, "DAYTIME", "lane_change", 2),
                
                // 금요일 (가장 많은 데이터)
                createEventPattern(5, "EVENING", "hard_brake", 8),
                createEventPattern(5, "COMMUTE_FROM_WORK", "rapid_accel", 6),
                createEventPattern(5, "EVENING", "lane_change", 7)
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
}