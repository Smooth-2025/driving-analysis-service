package com.smooth.driving_analysis_service.reports.behavior.service;

import com.smooth.driving_analysis_service.reports.behavior.dto.projection.EventPatternProjection;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.DrivingPatternDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("BehaviorPatternAnalyzer 단위 테스트")
class BehaviorPatternAnalyzerTest {

    @InjectMocks
    private BehaviorPatternAnalyzer behaviorPatternAnalyzer;

    private List<EventPatternProjection> mockEventPatterns;

    @BeforeEach
    void setUp() {
        mockEventPatterns = new ArrayList<>();
        
        // 금요일 저녁에 가장 많은 위험행동 패턴
        mockEventPatterns.add(createMockProjection(5, "EVENING", "hard_brake", 8));
        mockEventPatterns.add(createMockProjection(5, "EVENING", "rapid_accel", 6));
        mockEventPatterns.add(createMockProjection(5, "EVENING", "lane_change", 7));
        
        // 평일 퇴근시간 급제동 패턴
        mockEventPatterns.add(createMockProjection(1, "COMMUTE_FROM_WORK", "hard_brake", 3));
        mockEventPatterns.add(createMockProjection(2, "COMMUTE_FROM_WORK", "hard_brake", 4));
        mockEventPatterns.add(createMockProjection(3, "COMMUTE_FROM_WORK", "hard_brake", 2));
        mockEventPatterns.add(createMockProjection(4, "COMMUTE_FROM_WORK", "hard_brake", 5));
        
        // 출근시간 급가속 패턴
        mockEventPatterns.add(createMockProjection(1, "COMMUTE_TO_WORK", "rapid_accel", 2));
        mockEventPatterns.add(createMockProjection(2, "COMMUTE_TO_WORK", "rapid_accel", 3));
        mockEventPatterns.add(createMockProjection(3, "COMMUTE_TO_WORK", "rapid_accel", 1));
        mockEventPatterns.add(createMockProjection(4, "COMMUTE_TO_WORK", "rapid_accel", 4));
    }

    @Test
    @DisplayName("정상적인 이벤트 패턴 분석")
    void analyzeDrivingPattern_Success() {
        // when
        DrivingPatternDto result = behaviorPatternAnalyzer.analyzeDrivingPattern(mockEventPatterns);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getWeekday()).isEqualTo("금");
        assertThat(result.getTimeslot()).isEqualTo("저녁");
        assertThat(result.getChart()).hasSize(7); // 월~일
        assertThat(result.getComment()).isNotBlank();
    }

    @Test
    @DisplayName("빈 이벤트 패턴 처리")
    void analyzeDrivingPattern_EmptyPatterns() {
        // given
        List<EventPatternProjection> emptyPatterns = new ArrayList<>();

        // when
        DrivingPatternDto result = behaviorPatternAnalyzer.analyzeDrivingPattern(emptyPatterns);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getWeekday()).isEqualTo("금요일");
        assertThat(result.getTimeslot()).isEqualTo("저녁");
        assertThat(result.getChart()).hasSize(7);
        assertThat(result.getComment()).contains("충분한 주행 데이터가 없어");
    }

    @Test
    @DisplayName("요일별 최다 발생 시간대 계산")
    void analyzeDrivingPattern_WeekdayTimeSlotCalculation() {
        // when
        DrivingPatternDto result = behaviorPatternAnalyzer.analyzeDrivingPattern(mockEventPatterns);

        // then
        assertThat(result.getChart()).hasSize(7);
        
        // 월요일 차트 검증 (퇴근시간 급제동 3회, 출근시간 급가속 2회)
        DrivingPatternDto.WeeklyChartDto mondayChart = result.getChart().get(0);
        assertThat(mondayChart.getWeekday()).isEqualTo("월");
        assertThat(mondayChart.getActions().getHardBrake().getTimeSlot()).isEqualTo("퇴근");
        assertThat(mondayChart.getActions().getHardBrake().getCount()).isEqualTo(3);
        assertThat(mondayChart.getActions().getRapidAccel().getTimeSlot()).isEqualTo("출근");
        assertThat(mondayChart.getActions().getRapidAccel().getCount()).isEqualTo(2);
        
        // 금요일 차트 검증 (저녁시간 모든 행동 최다)
        DrivingPatternDto.WeeklyChartDto fridayChart = result.getChart().get(4);
        assertThat(fridayChart.getWeekday()).isEqualTo("금");
        assertThat(fridayChart.getActions().getHardBrake().getTimeSlot()).isEqualTo("저녁");
        assertThat(fridayChart.getActions().getHardBrake().getCount()).isEqualTo(8);
        assertThat(fridayChart.getActions().getRapidAccel().getTimeSlot()).isEqualTo("저녁");
        assertThat(fridayChart.getActions().getRapidAccel().getCount()).isEqualTo(6);
        assertThat(fridayChart.getActions().getLaneChange().getTimeSlot()).isEqualTo("저녁");
        assertThat(fridayChart.getActions().getLaneChange().getCount()).isEqualTo(7);
    }

    @Test
    @DisplayName("전체 최다 발생 요일/시간대 계산")
    void analyzeDrivingPattern_OverallPeakCalculation() {
        // when
        DrivingPatternDto result = behaviorPatternAnalyzer.analyzeDrivingPattern(mockEventPatterns);

        // then
        // 금요일 저녁이 전체적으로 가장 위험행동이 많음 (8+6+7=21회)
        assertThat(result.getWeekday()).isEqualTo("금");
        assertThat(result.getTimeslot()).isEqualTo("저녁");
    }

    @Test
    @DisplayName("코멘트 생성 로직")
    void analyzeDrivingPattern_CommentGeneration() {
        // when
        DrivingPatternDto result = behaviorPatternAnalyzer.analyzeDrivingPattern(mockEventPatterns);

        // then
        assertThat(result.getComment()).isNotBlank();
        assertThat(result.getComment()).contains("평일");
        assertThat(result.getComment()).contains("저녁");
    }

    private EventPatternProjection createMockProjection(int weekday, String timeSlot, String eventType, int count) {
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