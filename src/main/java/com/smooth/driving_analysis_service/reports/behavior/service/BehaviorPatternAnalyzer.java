package com.smooth.driving_analysis_service.reports.behavior.service;

import com.smooth.driving_analysis_service.reports.behavior.dto.projection.EventPatternProjectionDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.DrivingPatternDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 위험행동 패턴 분석 로직을 담당하는 컴포넌트
 */
@Component
@Slf4j
public class BehaviorPatternAnalyzer {

    private static final Map<Integer, String> WEEKDAY_MAP = Map.of(
            1, "월", 2, "화", 3, "수", 4, "목", 5, "금", 6, "토", 7, "일"
    );

    private static final Map<String, String> TIMESLOT_MAP = Map.of(
            "DAWN", "새벽",
            "COMMUTE_TO_WORK", "출근", 
            "DAYTIME", "낮",
            "COMMUTE_FROM_WORK", "퇴근",
            "EVENING", "저녁"
    );

    private static final List<String> TIMESLOT_PRIORITY = List.of(
            "DAWN", "DAYTIME", "EVENING", "COMMUTE_TO_WORK", "COMMUTE_FROM_WORK"
    );

    /**
     * 이벤트 패턴 데이터를 분석하여 DrivingPattern DTO로 변환
     */
    public DrivingPatternDto analyzeDrivingPattern(List<EventPatternProjectionDto> eventPatterns) {
        if (eventPatterns.isEmpty()) {
            return createEmptyPattern();
        }

        // 1. 요일별 차트 데이터 생성
        List<DrivingPatternDto.WeeklyChartDto> weeklyCharts = generateWeeklyCharts(eventPatterns);

        // 2. 전체적으로 가장 빈번한 패턴 찾기
        String dominantWeekday = findDominantWeekday(eventPatterns);
        String dominantTimeSlot = findDominantTimeSlot(eventPatterns);

        // 3. 코멘트 생성
        String comment = generateComment(eventPatterns, dominantWeekday, dominantTimeSlot);

        return DrivingPatternDto.builder()
                .weekday(dominantWeekday)
                .timeslot(dominantTimeSlot)
                .chart(weeklyCharts)
                .comment(comment)
                .build();
    }

    private List<DrivingPatternDto.WeeklyChartDto> generateWeeklyCharts(List<EventPatternProjectionDto> eventPatterns) {
        List<DrivingPatternDto.WeeklyChartDto> charts = new ArrayList<>();

        // 요일별로 그룹핑
        Map<Integer, List<EventPatternProjectionDto>> weekdayGroups = eventPatterns.stream()
                .collect(Collectors.groupingBy(EventPatternProjectionDto::getWeekday));

        // 월~일 순서로 처리
        for (int weekday = 1; weekday <= 7; weekday++) {
            String weekdayName = WEEKDAY_MAP.get(weekday);
            List<EventPatternProjectionDto> weekdayEvents = weekdayGroups.getOrDefault(weekday, new ArrayList<>());

            DrivingPatternDto.ActionsDto actions = generateActionsForWeekday(weekdayEvents);

            charts.add(DrivingPatternDto.WeeklyChartDto.builder()
                    .weekday(weekdayName)
                    .actions(actions)
                    .build());
        }

        return charts;
    }

    private DrivingPatternDto.ActionsDto generateActionsForWeekday(List<EventPatternProjectionDto> weekdayEvents) {
        // 행동별로 그룹핑하여 가장 많이 발생한 시간대 찾기
        Map<String, List<EventPatternProjectionDto>> actionGroups = weekdayEvents.stream()
                .collect(Collectors.groupingBy(EventPatternProjectionDto::getEventType));

        return DrivingPatternDto.ActionsDto.builder()
                .hardBrake(findDominantTimeSlotForAction(actionGroups.getOrDefault("hard_brake", new ArrayList<>())))
                .rapidAccel(findDominantTimeSlotForAction(actionGroups.getOrDefault("rapid_accel", new ArrayList<>())))
                .laneChange(findDominantTimeSlotForAction(actionGroups.getOrDefault("lane_change", new ArrayList<>())))
                .build();
    }

    private DrivingPatternDto.ActionTimeSlotDto findDominantTimeSlotForAction(List<EventPatternProjectionDto> actionEvents) {
        if (actionEvents.isEmpty()) {
            return null; // 데이터 없음
        }

        // 시간대별 합계 계산
        Map<String, Integer> timeSlotCounts = actionEvents.stream()
                .collect(Collectors.groupingBy(
                        EventPatternProjectionDto::getTimeSlot,
                        Collectors.summingInt(EventPatternProjectionDto::getEventCount)
                ));

        // 가장 많이 발생한 시간대 찾기 (동률시 우선순위 적용)
        String dominantTimeSlot = timeSlotCounts.entrySet().stream()
                .max((e1, e2) -> {
                    int countCompare = e1.getValue().compareTo(e2.getValue());
                    if (countCompare != 0) return countCompare;
                    // 동률시 우선순위 적용
                    return Integer.compare(
                            TIMESLOT_PRIORITY.indexOf(e2.getKey()),
                            TIMESLOT_PRIORITY.indexOf(e1.getKey())
                    );
                })
                .map(Map.Entry::getKey)
                .orElse("DAYTIME");

        return DrivingPatternDto.ActionTimeSlotDto.builder()
                .timeSlot(TIMESLOT_MAP.get(dominantTimeSlot))
                .count(timeSlotCounts.get(dominantTimeSlot))
                .build();
    }

    private String findDominantWeekday(List<EventPatternProjectionDto> eventPatterns) {
        Map<Integer, Integer> weekdayCounts = eventPatterns.stream()
                .collect(Collectors.groupingBy(
                        EventPatternProjectionDto::getWeekday,
                        Collectors.summingInt(EventPatternProjectionDto::getEventCount)
                ));

        return weekdayCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> WEEKDAY_MAP.get(entry.getKey()))
                .orElse("금요일");
    }

    private String findDominantTimeSlot(List<EventPatternProjectionDto> eventPatterns) {
        Map<String, Integer> timeSlotCounts = eventPatterns.stream()
                .collect(Collectors.groupingBy(
                        EventPatternProjectionDto::getTimeSlot,
                        Collectors.summingInt(EventPatternProjectionDto::getEventCount)
                ));

        String dominantTimeSlot = timeSlotCounts.entrySet().stream()
                .max((e1, e2) -> {
                    int countCompare = e1.getValue().compareTo(e2.getValue());
                    if (countCompare != 0) return countCompare;
                    // 동률시 우선순위 적용
                    return Integer.compare(
                            TIMESLOT_PRIORITY.indexOf(e2.getKey()),
                            TIMESLOT_PRIORITY.indexOf(e1.getKey())
                    );
                })
                .map(Map.Entry::getKey)
                .orElse("EVENING");

        return TIMESLOT_MAP.get(dominantTimeSlot);
    }

    private String generateComment(List<EventPatternProjectionDto> eventPatterns, String dominantWeekday, String dominantTimeSlot) {
        // 행동별 주요 시간대 분석
        Map<String, String> actionTimeSlots = new HashMap<>();
        
        Map<String, List<EventPatternProjectionDto>> actionGroups = eventPatterns.stream()
                .collect(Collectors.groupingBy(EventPatternProjectionDto::getEventType));

        actionGroups.forEach((action, events) -> {
            Map<String, Integer> timeSlotCounts = events.stream()
                    .collect(Collectors.groupingBy(
                            EventPatternProjectionDto::getTimeSlot,
                            Collectors.summingInt(EventPatternProjectionDto::getEventCount)
                    ));

            String dominantSlot = timeSlotCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(entry -> TIMESLOT_MAP.get(entry.getKey()))
                    .orElse("낮");

            actionTimeSlots.put(action, dominantSlot);
        });

        return String.format("%s %s에는 급제동과 급가속이 늘어나는 패턴이 보여요! %s 운전 시 조금 더 여유 있는 주행을 해보세요.",
                dominantWeekday.contains("토") || dominantWeekday.contains("일") ? "주말" : "평일",
                dominantTimeSlot,
                dominantTimeSlot.equals("퇴근") ? "퇴근길" : dominantTimeSlot + " 시간대");
    }

    private DrivingPatternDto createEmptyPattern() {
        List<DrivingPatternDto.WeeklyChartDto> emptyCharts = new ArrayList<>();
        
        for (int weekday = 1; weekday <= 7; weekday++) {
            emptyCharts.add(DrivingPatternDto.WeeklyChartDto.builder()
                    .weekday(WEEKDAY_MAP.get(weekday))
                    .actions(DrivingPatternDto.ActionsDto.builder()
                            .hardBrake(null)
                            .rapidAccel(null)
                            .laneChange(null)
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