package com.smooth.driving_analysis_service.reports.behavior.service;

import com.smooth.driving_analysis_service.reports.behavior.dto.projection.EventPatternProjection;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.DrivingPatternDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("BehaviorPatternAnalyzer 단위 테스트")
class BehaviorPatternAnalyzerTest {

    @InjectMocks
    private BehaviorPatternAnalyzer patternAnalyzer;

    @Test
    @DisplayName("정상적인 이벤트 패턴 데이터로 분석")
    void analyzeDrivingPattern_Success() {
        // given
        List<EventPatternProjection> eventPatterns = List.of(
                createEventPattern(5, "EVENING", "hard_brake", 8),
                createEventPattern(5, "COMMUTE_FROM_WORK", "rapid_accel", 6),
                createEventPattern(1, "COMMUTE_TO_WORK", "lane_change", 3)
        );

        // when
        DrivingPatternDto result = patternAnalyzer.analyzeDrivingPattern(eventPatterns);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getWeekday()).isEqualTo("금");
        assertThat(result.getTimeslot()).isEqualTo("저녁");
        assertThat(result.getChart()).hasSize(7); // 월~일
        assertThat(result.getComment()).isNotEmpty();
    }

    @Test
    @DisplayName("빈 데이터일 때 기본 패턴 반환")
    void analyzeDrivingPattern_EmptyData() {
        // given
        List<EventPatternProjection> emptyPatterns = List.of();

        // when
        DrivingPatternDto result = patternAnalyzer.analyzeDrivingPattern(emptyPatterns);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getWeekday()).isEqualTo("금요일");
        assertThat(result.getTimeslot()).isEqualTo("저녁");
        assertThat(result.getChart()).hasSize(7);
        assertThat(result.getComment()).contains("충분한 주행 데이터가 없어");
    }

    @Test
    @DisplayName("주말 패턴 분석")
    void analyzeDrivingPattern_WeekendPattern() {
        // given
        List<EventPatternProjection> weekendPatterns = List.of(
                createEventPattern(6, "DAYTIME", "hard_brake", 10),
                createEventPattern(7, "EVENING", "rapid_accel", 8)
        );

        // when
        DrivingPatternDto result = patternAnalyzer.analyzeDrivingPattern(weekendPatterns);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getWeekday()).isIn("토", "일");
        assertThat(result.getComment()).contains("주말");
    }

    @Test
    @DisplayName("시간대별 우선순위 적용")
    void analyzeDrivingPattern_TimeSlotPriority() {
        // given - 동일한 카운트로 우선순위 테스트
        List<EventPatternProjection> sameCountPatterns = List.of(
                createEventPattern(1, "DAWN", "hard_brake", 5),
                createEventPattern(1, "EVENING", "hard_brake", 5),
                createEventPattern(1, "DAYTIME", "hard_brake", 5)
        );

        // when
        DrivingPatternDto result = patternAnalyzer.analyzeDrivingPattern(sameCountPatterns);

        // then
        assertThat(result).isNotNull();
        // 우선순위: DAWN > DAYTIME > EVENING > COMMUTE_TO_WORK > COMMUTE_FROM_WORK
        assertThat(result.getTimeslot()).isEqualTo("새벽");
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