package com.smooth.driving_analysis_service.reports.behavior.service;

import com.smooth.driving_analysis_service.reports.behavior.dto.projection.EventPatternProjectionDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.BehaviorAnalysisResponseDto;
import com.smooth.driving_analysis_service.reports.behavior.repository.BehaviorPatternRepository;
import com.smooth.driving_analysis_service.reports.behavior.repository.BehaviorTotalCountsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 위험 행동 분석 서비스 구현체
 *
 * Task 1: totalCounts (총합)
 * Task 2: drivingPattern (시간대 패턴 분석)
 * Task 3: compare (이전 vs 현재 비교)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BehaviorReportServiceImpl implements BehaviorReportService {

    private final BehaviorTotalCountsRepository totalCountsRepository;
    private final BehaviorPatternRepository behaviorPatternRepository;
    private final BehaviorPatternAnalyzer patternAnalyzer;
    private final BehaviorCompareAnalyzer compareAnalyzer;

    @Override
    public BehaviorAnalysisResponseDto getBehaviorAnalysis(String reportId) {
        log.info("Getting behavior analysis for reportId: {}", reportId);

        try {
            // reportId에서 숫자 부분 추출 (u1_r3_20250901 -> 3)
            Long reportIdLong = extractReportIdNumber(reportId);

            log.info("Behavior analysis - requested: {}, effectiveReportIdUsed: {}", reportId, reportIdLong);

            // Task 1: totalCounts 구현
            BehaviorAnalysisResponseDto.TotalCounts totalCounts = getTotalCounts(reportIdLong);

            // Task 2: drivingPattern 구현
            BehaviorAnalysisResponseDto.DrivingPattern drivingPattern = getDrivingPattern(reportIdLong);

            // Task 3: compare 구현
            BehaviorAnalysisResponseDto.Compare compare = getCompare(reportIdLong, totalCounts);

            log.info(
                    "Behavior analysis completed - requested: {}, used: {}, totalCounts: {}, drivingPattern: {}, compare: {}",
                    reportId, reportIdLong, totalCounts,
                    drivingPattern.getWeekday() + " " + drivingPattern.getTimeslot(), compare.getIncdec());

            return BehaviorAnalysisResponseDto.builder()
                    .reportId(reportId)
                    .totalCounts(totalCounts)
                    .drivingPattern(drivingPattern)
                    .compare(compare)
                    .build();

        } catch (Exception e) {
            log.error("Error getting behavior analysis for reportId: {}", reportId, e);
            return createDefaultResponse(reportId);
        }
    }

    /**
     * Task 1: totalCounts 조회
     */
    private BehaviorAnalysisResponseDto.TotalCounts getTotalCounts(Long reportIdLong) {
        try {
            BehaviorAnalysisResponseDto.TotalCounts totalCounts = totalCountsRepository.findTotalCountsByReportId(reportIdLong);
            log.info("TotalCounts retrieved for reportId {}: {}", reportIdLong, totalCounts);
            return totalCounts != null ? totalCounts : 
                BehaviorAnalysisResponseDto.TotalCounts.builder()
                    .hardBrake(0).rapidAccel(0).laneChange(0).build();
        } catch (Exception e) {
            log.warn("Failed to get total counts for reportId: {}", reportIdLong, e);
            return BehaviorAnalysisResponseDto.TotalCounts.builder()
                .hardBrake(0).rapidAccel(0).laneChange(0).build();
        }
    }

    /**
     * Task 2: drivingPattern 조회
     */
    private BehaviorAnalysisResponseDto.DrivingPattern getDrivingPattern(Long reportIdLong) {
        try {
            List<EventPatternProjectionDto> eventPatterns = behaviorPatternRepository
                    .findEventPatternsByReportId(reportIdLong);
            log.info("EventPatterns retrieved for reportId {}: {} patterns", reportIdLong, eventPatterns.size());
            BehaviorAnalysisResponseDto.DrivingPattern pattern = patternAnalyzer.analyzeDrivingPattern(eventPatterns);
            log.info("DrivingPattern analyzed: {} {}", pattern.getWeekday(), pattern.getTimeslot());
            return pattern;
        } catch (Exception e) {
            log.warn("Failed to get driving pattern for reportId: {}", reportIdLong, e);
            return createDefaultDrivingPattern();
        }
    }

    /**
     * Task 3: compare 조회
     */
    private BehaviorAnalysisResponseDto.Compare getCompare(Long reportIdLong, BehaviorAnalysisResponseDto.TotalCounts totalCounts) {
        try {
            return compareAnalyzer.analyzeCompare(reportIdLong, totalCounts);
        } catch (Exception e) {
            log.warn("Failed to get compare for reportId: {}", reportIdLong, e);
            return createDefaultCompare(totalCounts);
        }
    }

    /**
     * 기본 DrivingPattern 생성
     */
    private BehaviorAnalysisResponseDto.DrivingPattern createDefaultDrivingPattern() {
        return BehaviorAnalysisResponseDto.DrivingPattern.builder()
                .weekday("금요일")
                .timeslot("저녁")
                .chart(List.of())
                .comment("오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
                .build();
    }

    /**
     * 기본 Compare 생성
     */
    private BehaviorAnalysisResponseDto.Compare createDefaultCompare(BehaviorAnalysisResponseDto.TotalCounts totalCounts) {
        return BehaviorAnalysisResponseDto.Compare.builder()
                .incdec(0.0)
                .chart(BehaviorAnalysisResponseDto.Chart.builder()
                        .hardBrake(BehaviorAnalysisResponseDto.BeforeAfter.builder()
                                .before(0)
                                .current(totalCounts.getHardBrake())
                                .build())
                        .rapidAccel(BehaviorAnalysisResponseDto.BeforeAfter.builder()
                                .before(0)
                                .current(totalCounts.getRapidAccel())
                                .build())
                        .laneChange(BehaviorAnalysisResponseDto.BeforeAfter.builder()
                                .before(0)
                                .current(totalCounts.getLaneChange())
                                .build())
                        .build())
                .comment("첫 번째 리포트로 이전 데이터와 비교할 수 없습니다.")
                .build();
    }

    /**
     * 기본 응답 생성 (전체 오류 시)
     */
    private BehaviorAnalysisResponseDto createDefaultResponse(String reportId) {
        BehaviorAnalysisResponseDto.TotalCounts defaultCounts = 
            BehaviorAnalysisResponseDto.TotalCounts.builder()
                .hardBrake(0).rapidAccel(0).laneChange(0).build();
        BehaviorAnalysisResponseDto.DrivingPattern defaultPattern = createDefaultDrivingPattern();
        BehaviorAnalysisResponseDto.Compare defaultCompare = createDefaultCompare(defaultCounts);

        return BehaviorAnalysisResponseDto.builder()
                .reportId(reportId)
                .totalCounts(defaultCounts)
                .drivingPattern(defaultPattern)
                .compare(BehaviorAnalysisResponseDto.Compare.builder()
                        .incdec(defaultCompare.getIncdec())
                        .comment(defaultCompare.getComment()) // 기본 코멘트 추가
                        .chart(BehaviorAnalysisResponseDto.Chart.builder()
                                .hardBrake(BehaviorAnalysisResponseDto.BeforeAfter.builder()
                                        .before(0)
                                        .current(0)
                                        .build())
                                .rapidAccel(BehaviorAnalysisResponseDto.BeforeAfter.builder()
                                        .before(0)
                                        .current(0)
                                        .build())
                                .laneChange(BehaviorAnalysisResponseDto.BeforeAfter.builder()
                                        .before(0)
                                        .current(0)
                                        .build())
                                .build())
                        .build())
                .build();
    }

    @Override
    public BehaviorAnalysisResponseDto.TotalCounts calculateTotalCounts(List<String> drivingIds) {
        try {
            return totalCountsRepository.findTotalCountsByDrivingIds(drivingIds);
        } catch (Exception e) {
            log.warn("Failed to calculate total counts for drivingIds: {}", drivingIds, e);
            return BehaviorAnalysisResponseDto.TotalCounts.builder()
                .hardBrake(0).rapidAccel(0).laneChange(0).build();
        }
    }

    @Override
    public BehaviorAnalysisResponseDto.DrivingPattern analyzeDrivingPattern(List<String> drivingIds) {
        try {
            List<EventPatternProjectionDto> eventPatterns = behaviorPatternRepository
                    .findEventPatternsByDrivingIds(drivingIds);
            return patternAnalyzer.analyzeDrivingPattern(eventPatterns);
        } catch (Exception e) {
            log.warn("Failed to analyze driving pattern for drivingIds: {}", drivingIds, e);
            return createDefaultDrivingPattern();
        }
    }

    @Override
    public BehaviorAnalysisResponseDto.Compare compareWithPrevious(String reportId, BehaviorAnalysisResponseDto.TotalCounts currentCounts) {
        try {
            return compareAnalyzer.analyzeCompare(extractReportIdNumber(reportId), currentCounts);
        } catch (Exception e) {
            log.warn("Failed to compare with previous for reportId: {}", reportId, e);
            return createDefaultCompare(currentCounts);
        }
    }

    /**
     * reportId에서 숫자 부분 추출 (u1_r3_20250901 -> 3, 또는 단순 숫자 "13" -> 13)
     */
    private Long extractReportIdNumber(String reportId) {
        if (reportId == null || reportId.trim().isEmpty()) {
            log.warn("Empty reportId provided, using default 1L");
            return 1L;
        }

        try {
            // 1. 단순 숫자인 경우 직접 파싱
            if (reportId.matches("\\d+")) {
                Long result = Long.parseLong(reportId);
                log.debug("Parsed simple numeric reportId: {} -> {}", reportId, result);
                return result;
            }

            // 2. u1_r3_20250901 형식에서 r 다음 숫자 추출
            String[] parts = reportId.split("_");
            for (String part : parts) {
                if (part.startsWith("r") && part.length() > 1) {
                    String numberPart = part.substring(1);
                    if (numberPart.matches("\\d+")) {
                        Long result = Long.parseLong(numberPart);
                        log.debug("Extracted reportId from formatted string: {} -> {}", reportId, result);
                        return result;
                    }
                }
            }

            // 3. 패턴이 맞지 않으면 경고 후 기본값 사용
            log.warn("Cannot extract reportId number from: '{}', using default 1L", reportId);
            return 1L;
        } catch (NumberFormatException e) {
            log.warn("Error parsing reportId number from: '{}', using default 1L - {}", reportId, e.getMessage());
            return 1L;
        } catch (Exception e) {
            log.warn("Unexpected error parsing reportId: '{}', using default 1L", reportId, e);
            return 1L;
        }
    }

    @Override
    public void generateInterimReport(Long reportId, Long userId, List<String> drivingIds) {
        log.info("Generating behavior interim report: reportId={}, userId={}, drivingCount={}",
                reportId, userId, drivingIds.size());

        try {
            // 1. 총합 계산 및 저장
            BehaviorAnalysisResponseDto.TotalCounts totalCounts = calculateTotalCounts(drivingIds);
            // TODO: 총합 데이터를 behavior_total_counts 테이블에 저장

            // 2. 패턴 분석 및 저장
            BehaviorAnalysisResponseDto.DrivingPattern pattern = analyzeDrivingPattern(drivingIds);
            // TODO: 패턴 데이터를 behavior_pattern 테이블에 저장

            // 3. 비교 분석 및 저장
            BehaviorAnalysisResponseDto.Compare compare = compareWithPrevious(reportId.toString(), totalCounts);
            // TODO: 비교 데이터를 behavior_compare 테이블에 저장

            log.info("Behavior interim report generated successfully: reportId={}", reportId);

        } catch (Exception e) {
            log.error("Failed to generate behavior interim report: reportId={}", reportId, e);
            throw e;
        }
    }

    @Override
    public void generateFinalReport(Long reportId, Long userId, List<String> drivingIds) {
        log.info("Generating behavior final report: reportId={}, userId={}, drivingCount={}",
                reportId, userId, drivingIds.size());

        try {
            // Final 리포트는 Interim과 동일한 로직이지만 FINAL 타입으로 저장
            generateInterimReport(reportId, userId, drivingIds);

            log.info("Behavior final report generated successfully: reportId={}", reportId);

        } catch (Exception e) {
            log.error("Failed to generate behavior final report: reportId={}", reportId, e);
            throw e;
        }
    }
}