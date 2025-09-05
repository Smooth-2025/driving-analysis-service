package com.smooth.driving_analysis_service.reports.behavior.service;

import com.smooth.driving_analysis_service.reports.behavior.dto.projection.EventPatternProjection;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.BehaviorAnalysisResponseDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.CompareDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.DrivingPatternDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.TotalCountsDto;
import com.smooth.driving_analysis_service.reports.behavior.repository.BehaviorPatternRepository;
import com.smooth.driving_analysis_service.reports.behavior.repository.BehaviorTotalCountsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BehaviorReportServiceImpl implements BehaviorReportService {

    private final BehaviorTotalCountsRepository totalCountsRepository;
    private final BehaviorPatternRepository behaviorPatternRepository;
    private final BehaviorPatternAnalyzer patternAnalyzer;
    private final BehaviorCompareAnalyzer compareAnalyzer;

    @Override
    public BehaviorAnalysisResponseDto getBehaviorAnalysis(String reportId) {
        log.info("Task 1+2+3: 전체 위험운전 행동 분석 조회 - reportId: {}", reportId);

        try {
            // reportId에서 숫자 부분 추출 (예: "u1_r3_20250901" -> 3)
            Long reportIdLong = extractReportIdFromString(reportId);

            // Task 1: totalCounts 조회
            TotalCountsDto totalCounts = totalCountsRepository.findTotalCountsByReportId(reportIdLong);
            if (totalCounts == null) {
                log.warn("리포트 데이터 없음 - reportId: {}", reportId);
                totalCounts = TotalCountsDto.of(0, 0, 0);
            }

            // Task 2: drivingPattern 조회 및 분석
            List<EventPatternProjection> eventPatterns = behaviorPatternRepository.findEventPatternsByReportId(reportIdLong);
            DrivingPatternDto drivingPattern = patternAnalyzer.analyzeDrivingPattern(eventPatterns);

            // Task 3: compare 분석
            CompareDto compare = compareAnalyzer.analyzeCompare(reportIdLong, totalCounts);

            // 응답 생성
            return BehaviorAnalysisResponseDto.builder()
                    .reportId(reportId)
                    .totalCounts(BehaviorAnalysisResponseDto.TotalCounts.builder()
                            .hardBrake(totalCounts.getHardBrake())
                            .rapidAccel(totalCounts.getRapidAccel())
                            .laneChange(totalCounts.getLaneChange())
                            .total(totalCounts.getTotal())
                            .build())
                    .drivingPattern(convertToDrivingPattern(drivingPattern))
                    .compare(convertToCompare(compare))
                    .build();

        } catch (Exception e) {
            log.error("위험운전 행동 분석 조회 실패 - reportId: {}", reportId, e);
            // 오류 시 기본 데이터 반환
            return createDefaultResponse(reportId);
        }
    }

    private BehaviorAnalysisResponseDto.DrivingPattern convertToDrivingPattern(DrivingPatternDto dto) {
        List<BehaviorAnalysisResponseDto.WeeklyChart> charts = dto.getChart().stream()
                .map(this::convertToWeeklyChart)
                .toList();

        return BehaviorAnalysisResponseDto.DrivingPattern.builder()
                .weekday(dto.getWeekday())
                .timeslot(dto.getTimeslot())
                .chart(charts)
                .comment(dto.getComment())
                .build();
    }

    private BehaviorAnalysisResponseDto.WeeklyChart convertToWeeklyChart(DrivingPatternDto.WeeklyChartDto dto) {
        return BehaviorAnalysisResponseDto.WeeklyChart.builder()
                .weekday(dto.getWeekday())
                .actions(BehaviorAnalysisResponseDto.Actions.builder()
                        .hardBrake(convertToActionTimeSlot(dto.getActions().getHardBrake()))
                        .rapidAccel(convertToActionTimeSlot(dto.getActions().getRapidAccel()))
                        .laneChange(convertToActionTimeSlot(dto.getActions().getLaneChange()))
                        .build())
                .build();
    }

    private BehaviorAnalysisResponseDto.ActionTimeSlot convertToActionTimeSlot(DrivingPatternDto.ActionTimeSlotDto dto) {
        if (dto == null) {
            return null; // 데이터 없음
        }
        return BehaviorAnalysisResponseDto.ActionTimeSlot.builder()
                .timeSlot(dto.getTimeSlot())
                .count(dto.getCount())
                .build();
    }

    private BehaviorAnalysisResponseDto.Compare convertToCompare(CompareDto dto) {
        return BehaviorAnalysisResponseDto.Compare.builder()
                .incdec(dto.getIncdec())
                .chart(BehaviorAnalysisResponseDto.Chart.builder()
                        .hardBrake(BehaviorAnalysisResponseDto.BeforeAfter.builder()
                                .before(dto.getChart().getHardBrake().getBefore())
                                .current(dto.getChart().getHardBrake().getCurrent())
                                .build())
                        .rapidAccel(BehaviorAnalysisResponseDto.BeforeAfter.builder()
                                .before(dto.getChart().getRapidAccel().getBefore())
                                .current(dto.getChart().getRapidAccel().getCurrent())
                                .build())
                        .laneChange(BehaviorAnalysisResponseDto.BeforeAfter.builder()
                                .before(dto.getChart().getLaneChange().getBefore())
                                .current(dto.getChart().getLaneChange().getCurrent())
                                .build())
                        .build())
                .build();
    }

    private BehaviorAnalysisResponseDto createDefaultResponse(String reportId) {
        return BehaviorAnalysisResponseDto.builder()
                .reportId(reportId)
                .totalCounts(BehaviorAnalysisResponseDto.TotalCounts.builder()
                        .hardBrake(0).rapidAccel(0).laneChange(0).total(0)
                        .build())
                .drivingPattern(BehaviorAnalysisResponseDto.DrivingPattern.builder()
                        .weekday("금요일")
                        .timeslot("저녁")
                        .chart(createEmptyWeeklyCharts())
                        .comment("데이터 조회 중 오류가 발생했습니다.")
                        .build())
                .compare(BehaviorAnalysisResponseDto.Compare.builder()
                        .incdec(0.0)
                        .chart(BehaviorAnalysisResponseDto.Chart.builder()
                                .hardBrake(BehaviorAnalysisResponseDto.BeforeAfter.builder().before(0).current(0).build())
                                .rapidAccel(BehaviorAnalysisResponseDto.BeforeAfter.builder().before(0).current(0).build())
                                .laneChange(BehaviorAnalysisResponseDto.BeforeAfter.builder().before(0).current(0).build())
                                .build())
                        .build())
                .build();
    }

    private List<BehaviorAnalysisResponseDto.WeeklyChart> createEmptyWeeklyCharts() {
        String[] weekdays = {"월", "화", "수", "목", "금", "토", "일"};
        return List.of(weekdays).stream()
                .map(weekday -> BehaviorAnalysisResponseDto.WeeklyChart.builder()
                        .weekday(weekday)
                        .actions(BehaviorAnalysisResponseDto.Actions.builder()
                                .hardBrake(null).rapidAccel(null).laneChange(null)
                                .build())
                        .build())
                .toList();
    }

    // === 유틸 메서드 ===
    
    private Long extractReportIdFromString(String reportId) {
        try {
            // "u1_r3_20250901" 형식에서 "r3" 부분의 숫자 추출
            String[] parts = reportId.split("_");
            for (String part : parts) {
                if (part.startsWith("r")) {
                    return Long.parseLong(part.substring(1));
                }
            }
            return 1L; // 기본값
        } catch (Exception e) {
            log.warn("reportId 파싱 실패, 기본값 사용: {}", reportId, e);
            return 1L;
        }
    }
}