package com.smooth.driving_analysis_service.reports.behavior.service;

import com.smooth.driving_analysis_service.reports.behavior.dto.request.BehaviorDiffRequestDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.*;
import com.smooth.driving_analysis_service.reports.behavior.dto.result.*;
import com.smooth.driving_analysis_service.reports.behavior.entity.*;
import com.smooth.driving_analysis_service.reports.behavior.repository.BehaviorDrivingRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BehaviorReportServiceImpl implements BehaviorReportService {

    private final BehaviorDrivingRecordRepository repo;

    @Override
    public BehaviorAnalysisResponseDto getBehaviorAnalysis(String reportId) {
        log.info("위험운전 행동 분석 조회 - reportId: {}", reportId);

        try {
            // reportId에서 숫자 부분 추출 (예: "u1_r3_20250901" -> 3)
            Long reportIdLong = extractReportIdFromString(reportId);

            // 현재 리포트 데이터 조회
            BehaviorSummaryResultDto currentData = fetchSummary(reportIdLong);

            // 이전 리포트 데이터 조회 (임시로 기본값)
            BehaviorSummaryResultDto prevData = getPreviousReportData(reportIdLong);

            // 총합 데이터
            BehaviorAnalysisResponseDto.TotalCounts totalCounts = BehaviorAnalysisResponseDto.TotalCounts.builder()
                    .hardBrake(currentData.getHardBrakeCount())
                    .rapidAccel(currentData.getRapidAccelCount())
                    .laneChange(currentData.getLaneChangeCount())
                    .total(currentData.getTotal())
                    .build();

            // 주행 패턴 분석
            BehaviorAnalysisResponseDto.DrivingPattern drivingPattern = generateDrivingPattern(reportIdLong);

            // 비교 분석
            BehaviorAnalysisResponseDto.Compare compare = generateCompareAnalysis(prevData, currentData);

            return BehaviorAnalysisResponseDto.builder()
                    .reportId(reportId)
                    .totalCounts(totalCounts)
                    .drivingPattern(drivingPattern)
                    .compare(compare)
                    .build();

        } catch (Exception e) {
            log.error("위험운전 행동 분석 조회 실패 - reportId: {}", reportId, e);
            // 오류 시 기본 데이터 반환
            return generateDefaultAnalysis(reportId);
        }
    }

    @Override
    @Transactional
    public void createOrUpdateInterimSnapshot(Long reportId) {
        log.info("INTERIM 스냅샷 생성/갱신 - reportId: {}", reportId);
        // TODO: 중간 스냅샷 생성 로직 구현
    }

    @Override
    @Transactional
    public void createFinalSnapshot(Long reportId) {
        log.info("FINAL 스냅샷 생성 - reportId: {}", reportId);
        // TODO: 최종 스냅샷 생성 로직 구현
    }

    @Override
    public BehaviorSummaryResponseDto getSummary(Long reportId) {
        BehaviorSummaryResultDto r = fetchSummary(reportId);

        return BehaviorSummaryResponseDto.builder()
                .hardBrakeCount(r.getHardBrakeCount())
                .rapidAccelCount(r.getRapidAccelCount())
                .laneChangeCount(r.getLaneChangeCount())
                .total(r.getTotal())
                .build();
    }

    @Override
    public BehaviorTrajectoryResponseDto getTrajectory(Long reportId) {
        List<TrajectoryPointResultDto> points = repo.findDominantPointsByItems(reportId)
                .stream()
                .map(r -> TrajectoryPointResultDto.builder()
                        .behavior(BehaviorType.from(String.valueOf(r[0])))
                        .dayOfWeek(((Number) r[1]).intValue())
                        .timeSlot(TimeSlot.from(String.valueOf(r[2])))
                        .count(((Number) r[3]).intValue())
                        .build())
                .collect(Collectors.toList());

        Map<BehaviorType, TimeSlot> representative = calcRepresentativeTimeSlot(points);

        return BehaviorTrajectoryResponseDto.builder()
                .dominantSlots(fillAllBehaviorDow(points))
                .insight(String.format(
                        "급제동: %s, 급가속: %s, 차선변경: %s",
                        toKorean(representative.get(BehaviorType.HARD_BRAKE)),
                        toKorean(representative.get(BehaviorType.RAPID_ACCEL)),
                        toKorean(representative.get(BehaviorType.LANE_CHANGE))))
                .build();
    }

    @Override
    public List<BehaviorDiffResponseDto> getDiff(Long reportId, BehaviorDiffRequestDto req) {
        BehaviorSummaryResultDto curr = fetchSummary(reportId);
        BehaviorSummaryResultDto prev = (req != null && req.getPrevReportId() != null)
                ? fetchSummary(req.getPrevReportId())
                : new BehaviorSummaryResultDto();

        return Arrays.asList(
                buildDiffResponse(BehaviorType.HARD_BRAKE, prev.getHardBrakeCount(), curr.getHardBrakeCount()),
                buildDiffResponse(BehaviorType.RAPID_ACCEL, prev.getRapidAccelCount(), curr.getRapidAccelCount()),
                buildDiffResponse(BehaviorType.LANE_CHANGE, prev.getLaneChangeCount(), curr.getLaneChangeCount()));
    }

    @Override
    public BehaviorCommentResponseDto getComment(Long reportId, BehaviorDiffRequestDto req) {
        BehaviorSummaryResultDto curr = fetchSummary(reportId);
        BehaviorSummaryResultDto prev = (req != null && req.getPrevReportId() != null)
                ? fetchSummary(req.getPrevReportId())
                : new BehaviorSummaryResultDto();

        String text = String.format("급제동 %s, 급가속 %s, 차선변경 %s",
                arrow(prev.getHardBrakeCount(), curr.getHardBrakeCount()),
                arrow(prev.getRapidAccelCount(), curr.getRapidAccelCount()),
                arrow(prev.getLaneChangeCount(), curr.getLaneChangeCount()));

        return BehaviorCommentResponseDto.builder().text(text).build();
    }

    // ---- private 유틸 메서드 (Impl 내부용) ----
    private BehaviorSummaryResultDto fetchSummary(Long reportId) {
        return repo.fetchSummary(reportId);
    }

    private BehaviorDiffResponseDto buildDiffResponse(BehaviorType type, int prev, int curr) {
        int diff = curr - prev;
        BehaviorDiffResponseDto.Direction direction = diff > 0 ? BehaviorDiffResponseDto.Direction.INCREASE
                : (diff < 0 ? BehaviorDiffResponseDto.Direction.DECREASE : BehaviorDiffResponseDto.Direction.FLAT);

        String pct = prev == 0 ? "-" : String.format("%.1f%%", ((double) diff / prev) * 100);

        return BehaviorDiffResponseDto.builder()
                .behavior(type)
                .prev(prev)
                .curr(curr)
                .diff(diff)
                .direction(direction)
                .pct(pct)
                .build();
    }

    private String arrow(int prev, int curr) {
        return curr > prev ? "▲" : curr < prev ? "▼" : "▬";
    }

    private List<BehaviorTrajectoryResponseDto.DominantSlot> fillAllBehaviorDow(List<TrajectoryPointResultDto> points) {
        // TODO: 기존 로직 그대로 구현
        return Collections.emptyList();
    }

    private Map<BehaviorType, TimeSlot> calcRepresentativeTimeSlot(List<TrajectoryPointResultDto> points) {
        // TODO: 기존 로직 그대로 구현
        return new HashMap<>();
    }

    private String toKorean(TimeSlot ts) {
        return ts == null ? "-" : ts.getKorean();
    }

    // === 새로운 분석 메서드들 ===

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
            return 1L;
        }
    }

    private BehaviorSummaryResultDto getPreviousReportData(Long reportId) {
        // TODO: 실제 이전 리포트 조회 로직 구현
        // 임시 데이터
        BehaviorSummaryResultDto prev = new BehaviorSummaryResultDto();
        prev.setHardBrakeCount(35);
        prev.setRapidAccelCount(40);
        prev.setLaneChangeCount(15);
        // prev.setTotal(90); // setTotal 메서드가 없으므로 주석 처리
        return prev;
    }

    private BehaviorAnalysisResponseDto.DrivingPattern generateDrivingPattern(Long reportId) {
        // 주간 차트 데이터 생성
        List<BehaviorAnalysisResponseDto.WeeklyChart> weeklyCharts = generateWeeklyCharts(reportId);

        // 가장 빈번한 패턴 분석
        String dominantWeekday = "금요일";
        String dominantTimeSlot = "저녁";
        String comment = "평일 저녁에는 급제동과 급회전이 늘어나는 패턴이 보여요! 퇴근길 운전 시 조금 더 여유 있는 주행을 해보세요.";

        return BehaviorAnalysisResponseDto.DrivingPattern.builder()
                .weekday(dominantWeekday)
                .timeslot(dominantTimeSlot)
                .chart(weeklyCharts)
                .comment(comment)
                .build();
    }

    private List<BehaviorAnalysisResponseDto.WeeklyChart> generateWeeklyCharts(Long reportId) {
        List<BehaviorAnalysisResponseDto.WeeklyChart> charts = new ArrayList<>();
        String[] weekdays = { "월", "화", "수", "목", "금", "토", "일" };

        for (String weekday : weekdays) {
            BehaviorAnalysisResponseDto.Actions actions = BehaviorAnalysisResponseDto.Actions.builder()
                    .hardBrake(BehaviorAnalysisResponseDto.ActionTimeSlot.builder()
                            .timeSlot("퇴근").count(5).build())
                    .rapidAccel(BehaviorAnalysisResponseDto.ActionTimeSlot.builder()
                            .timeSlot("출근").count(3).build())
                    .laneChange(BehaviorAnalysisResponseDto.ActionTimeSlot.builder()
                            .timeSlot("낮").count(2).build())
                    .build();

            charts.add(BehaviorAnalysisResponseDto.WeeklyChart.builder()
                    .weekday(weekday)
                    .actions(actions)
                    .build());
        }

        return charts;
    }

    private BehaviorAnalysisResponseDto.Compare generateCompareAnalysis(
            BehaviorSummaryResultDto prev, BehaviorSummaryResultDto current) {

        // 증감률 계산: (현재-이전)/이전 * 100
        double incdec = 0.0;
        int prevTotal = prev.getHardBrakeCount() + prev.getRapidAccelCount() + prev.getLaneChangeCount();
        int currentTotal = current.getTotal();

        if (prevTotal > 0) {
            incdec = ((double) (currentTotal - prevTotal) / prevTotal) * 100;
            incdec = Math.round(incdec * 100.0) / 100.0; // 소수점 2자리
        }

        // 바차트 데이터
        BehaviorAnalysisResponseDto.Chart chart = BehaviorAnalysisResponseDto.Chart.builder()
                .hardBrake(BehaviorAnalysisResponseDto.BeforeAfter.builder()
                        .before(prev.getHardBrakeCount())
                        .current(current.getHardBrakeCount())
                        .build())
                .rapidAccel(BehaviorAnalysisResponseDto.BeforeAfter.builder()
                        .before(prev.getRapidAccelCount())
                        .current(current.getRapidAccelCount())
                        .build())
                .laneChange(BehaviorAnalysisResponseDto.BeforeAfter.builder()
                        .before(prev.getLaneChangeCount())
                        .current(current.getLaneChangeCount())
                        .build())
                .build();

        return BehaviorAnalysisResponseDto.Compare.builder()
                .incdec(incdec)
                .chart(chart)
                .build();
    }

    private BehaviorAnalysisResponseDto generateDefaultAnalysis(String reportId) {
        // 기본 데이터
        BehaviorAnalysisResponseDto.TotalCounts totalCounts = BehaviorAnalysisResponseDto.TotalCounts.builder()
                .hardBrake(38).rapidAccel(42).laneChange(17).total(97).build();

        BehaviorAnalysisResponseDto.DrivingPattern drivingPattern = BehaviorAnalysisResponseDto.DrivingPattern.builder()
                .weekday("금요일").timeslot("저녁")
                .chart(generateWeeklyCharts(1L))
                .comment("평일 저녁에는 급제동과 급회전이 늘어나는 패턴이 보여요! 퇴근길 운전 시 조금 더 여유 있는 주행을 해보세요.")
                .build();

        BehaviorAnalysisResponseDto.Compare compare = BehaviorAnalysisResponseDto.Compare.builder()
                .incdec(1.04)
                .chart(BehaviorAnalysisResponseDto.Chart.builder()
                        .hardBrake(BehaviorAnalysisResponseDto.BeforeAfter.builder().before(35).current(38).build())
                        .rapidAccel(BehaviorAnalysisResponseDto.BeforeAfter.builder().before(40).current(42).build())
                        .laneChange(BehaviorAnalysisResponseDto.BeforeAfter.builder().before(15).current(17).build())
                        .build())
                .build();

        return BehaviorAnalysisResponseDto.builder()
                .reportId(reportId)
                .totalCounts(totalCounts)
                .drivingPattern(drivingPattern)
                .compare(compare)
                .build();
    }
}