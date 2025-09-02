package com.smooth.driving_analysis_service.reports.behavior.service;

import com.smooth.driving_analysis_service.reports.behavior.dto.request.BehaviorDiffRequestDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.*;
import com.smooth.driving_analysis_service.reports.behavior.dto.result.*;
import com.smooth.driving_analysis_service.reports.behavior.entity.*;
import com.smooth.driving_analysis_service.reports.behavior.repository.BehaviorDrivingRecordRepository;
import com.smooth.driving_analysis_service.reports.behavior.service.BehaviorReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BehaviorReportServiceImpl implements BehaviorReportService {

    private final BehaviorDrivingRecordRepository repo;

    @Override
    public BehaviorSummaryResponseDto getSummary(Long reportId) {
        BehaviorSummaryResultDto r = fetchSummary(reportId);

        return BehaviorSummaryResponseDto.builder()
                .text(String.format("급제동 %d회 · 급가속 %d회 · 차선변경 %d회 (총 %d회)",
                        r.getHardBrakeCount(), r.getRapidAccelCount(), r.getLaneChangeCount(), r.getTotal()))
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
                        toKorean(representative.get(BehaviorType.LANE_CHANGE))
                ))
                .build();
    }

    @Override
    public List<BehaviorDiffResponseDto> getDiff(Long reportId, BehaviorDiffRequestDto req) {
        BehaviorSummaryResultDto curr = fetchSummary(reportId);
        BehaviorSummaryResultDto prev = (req != null && req.getPrevReportId() != null) ?
                fetchSummary(req.getPrevReportId()) : new BehaviorSummaryResultDto();

        return Arrays.asList(
                buildDiffResponse(BehaviorType.HARD_BRAKE, prev.getHardBrakeCount(), curr.getHardBrakeCount()),
                buildDiffResponse(BehaviorType.RAPID_ACCEL, prev.getRapidAccelCount(), curr.getRapidAccelCount()),
                buildDiffResponse(BehaviorType.LANE_CHANGE, prev.getLaneChangeCount(), curr.getLaneChangeCount())
        );
    }

    @Override
    public BehaviorCommentResponseDto getComment(Long reportId, BehaviorDiffRequestDto req) {
        BehaviorSummaryResultDto curr = fetchSummary(reportId);
        BehaviorSummaryResultDto prev = (req != null && req.getPrevReportId() != null) ?
                fetchSummary(req.getPrevReportId()) : new BehaviorSummaryResultDto();

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
        DiffDirection dir = diff > 0 ? DiffDirection.UP : (diff < 0 ? DiffDirection.DOWN : DiffDirection.SAME);
        return BehaviorDiffResponseDto.builder()
                .behavior(type).prev(prev).curr(curr).diff(diff).direction(dir).build();
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
}
