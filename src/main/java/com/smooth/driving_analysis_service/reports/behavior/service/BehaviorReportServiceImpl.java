package com.smooth.driving_analysis_service.reports.behavior.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smooth.driving_analysis_service.reports.behavior.dto.projection.EventPatternProjectionDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.BehaviorAnalysisResponseDto;
import com.smooth.driving_analysis_service.reports.behavior.entity.BehaviorSnapshot;
import com.smooth.driving_analysis_service.reports.behavior.repository.BehaviorPatternRepository;
import com.smooth.driving_analysis_service.reports.behavior.repository.BehaviorSnapshotRepository;
import com.smooth.driving_analysis_service.reports.behavior.repository.BehaviorTotalCountsRepository;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private final BehaviorSnapshotRepository behaviorSnapshotRepository;
    private final MilestoneReportRepository milestoneReportRepository;
    private final BehaviorBatchService behaviorBatchService;
    private final ObjectMapper objectMapper;

    @Override
    public BehaviorAnalysisResponseDto getBehaviorAnalysis(String reportId) {
        log.info("Getting behavior analysis for reportId: {}", reportId);

        try {
            // 1. reportId로 MilestoneReport 조회
            Optional<MilestoneReport> reportOpt = milestoneReportRepository.findByReportId(reportId);
            if (reportOpt.isEmpty()) {
                log.error("MilestoneReport not found for reportId: {}", reportId);
                return createPendingResponse(reportId, "REPORT_NOT_FOUND");
            }
            
            MilestoneReport report = reportOpt.get();
            
            // 2. AC: 15회 이전 → 무조건 PENDING
            if (report.getNumberOfDriving() < 15) {
                log.info("numberOfDriving({}) < 15, returning PENDING for reportId: {}", 
                        report.getNumberOfDriving(), reportId);
                return createPendingResponse(reportId, "COLLECTING_DATA");
            }
            
            // 3. AC: 15회인데 스냅샷 없음 → 동기 on-demand 실행 한 번 시도
            Optional<BehaviorSnapshot> snapshotOpt = behaviorSnapshotRepository
                    .findByReportFkAndStatus(report.getId(), BehaviorSnapshot.Status.FINAL);
            
            if (snapshotOpt.isEmpty()) {
                log.info("No FINAL snapshot found for reportFk: {}, attempting on-demand generation", report.getId());
                
                try {
                    // on-demand 실행 (동기)
                    behaviorBatchService.materializeByDrivingIds(reportId, report.getUserId(), List.of());
                    
                    // 재조회
                    snapshotOpt = behaviorSnapshotRepository
                            .findByReportFkAndStatus(report.getId(), BehaviorSnapshot.Status.FINAL);
                    
                } catch (Exception e) {
                    log.error("On-demand generation failed for reportId: {}", reportId, e);
                }
                
                // 그래도 없으면 PENDING
                if (snapshotOpt.isEmpty()) {
                    return createPendingResponse(reportId, "ANALYSIS_IN_PROGRESS");
                }
            }
            
            // 4. 스냅샷 있으면 payload_json → DTO 매핑 반환
            BehaviorSnapshot snapshot = snapshotOpt.get();
            return convertSnapshotToResponse(reportId, snapshot);
            
        } catch (Exception e) {
            log.error("Error getting behavior analysis for reportId: {}", reportId, e);
            // AC: 에러 시에도 API는 200 + FALLBACK으로 안정 응답
            return createFallbackResponse(reportId);
        }
    }
    
    /**
     * AC: 15회 전 호출은 항상 success=true, code="PENDING" 등 표준 응답
     */
    private BehaviorAnalysisResponseDto createPendingResponse(String reportId, String reason) {
        return BehaviorAnalysisResponseDto.builder()
                .reportId(reportId)
                .status("PENDING")
                .message("데이터 수집 중입니다. 15회 주행 완료 후 분석 결과를 확인할 수 있습니다.")
                .reason(reason)
                .totalCounts(BehaviorAnalysisResponseDto.TotalCounts.builder()
                        .hardBrake(0).rapidAccel(0).laneChange(0).build())
                .drivingPattern(createDefaultDrivingPattern())
                .compare(createDefaultCompare(BehaviorAnalysisResponseDto.TotalCounts.builder()
                        .hardBrake(0).rapidAccel(0).laneChange(0).build()))
                .build();
    }
    
    /**
     * AC: 에러 시에도 200 + FALLBACK으로 안정 응답
     */
    private BehaviorAnalysisResponseDto createFallbackResponse(String reportId) {
        return BehaviorAnalysisResponseDto.builder()
                .reportId(reportId)
                .status("FALLBACK")
                .message("일시적인 오류로 기본 데이터를 제공합니다.")
                .totalCounts(BehaviorAnalysisResponseDto.TotalCounts.builder()
                        .hardBrake(5).rapidAccel(3).laneChange(2).build())
                .drivingPattern(createDefaultDrivingPattern())
                .compare(createDefaultCompare(BehaviorAnalysisResponseDto.TotalCounts.builder()
                        .hardBrake(5).rapidAccel(3).laneChange(2).build()))
                .build();
    }
    
    /**
     * 스냅샷 JSON을 DTO로 변환
     */
    private BehaviorAnalysisResponseDto convertSnapshotToResponse(String reportId, BehaviorSnapshot snapshot) {
        try {
            Map<String, Object> payload = objectMapper.readValue(snapshot.getPayloadJson(), Map.class);
            
            Map<String, Object> totalCountsMap = (Map<String, Object>) payload.get("totalCounts");
            Map<String, Object> patternMap = (Map<String, Object>) payload.get("drivingPattern");
            Map<String, Object> compareMap = (Map<String, Object>) payload.get("compare");
            
            BehaviorAnalysisResponseDto.TotalCounts totalCounts = BehaviorAnalysisResponseDto.TotalCounts.builder()
                    .hardBrake((Integer) totalCountsMap.get("hardBrakeCount"))
                    .rapidAccel((Integer) totalCountsMap.get("rapidAccelCount"))
                    .laneChange((Integer) totalCountsMap.get("laneChangeCount"))
                    .build();
            
            BehaviorAnalysisResponseDto.DrivingPattern drivingPattern = BehaviorAnalysisResponseDto.DrivingPattern.builder()
                    .weekday((String) patternMap.get("mostFrequentDay"))
                    .timeslot((String) patternMap.get("mostFrequentTimeSlot"))
                    .build();
            
            BehaviorAnalysisResponseDto.Compare compare = BehaviorAnalysisResponseDto.Compare.builder()
                    .incdec(((Number) compareMap.get("previousCycleRatio")).doubleValue())
                    .comment("이전 사이클 대비 분석 결과입니다.")
                    .build();
            
            return BehaviorAnalysisResponseDto.builder()
                    .reportId(reportId)
                    .status("SUCCESS")
                    .totalCounts(totalCounts)
                    .drivingPattern(drivingPattern)
                    .compare(compare)
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed to convert snapshot to response for reportId: {}", reportId, e);
            return createFallbackResponse(reportId);
        }
    }

    private BehaviorAnalysisResponseDto.TotalCounts getTotalCountsFromDB(Long reportId) {
        try {
            // driving_accumulated_stats에서 집계
            BehaviorTotalCountsRepository.TotalCountsProjection projection = totalCountsRepository.getTotalCountsByReportId(reportId);
            
            if (projection != null) {
                BehaviorAnalysisResponseDto.TotalCounts totalCounts = BehaviorAnalysisResponseDto.TotalCounts.builder()
                        .hardBrake(projection.getHardBrake() != null ? projection.getHardBrake() : 0)
                        .rapidAccel(projection.getRapidAccel() != null ? projection.getRapidAccel() : 0)
                        .laneChange(projection.getLaneChange() != null ? projection.getLaneChange() : 0)
                        .build();
                
                log.info("TotalCounts retrieved for reportId {}: hardBrake={}, rapidAccel={}, laneChange={}", 
                        reportId, totalCounts.getHardBrake(), totalCounts.getRapidAccel(), totalCounts.getLaneChange());
                return totalCounts;
            }
            
            log.warn("No projection data found for reportId: {}", reportId);
            return BehaviorAnalysisResponseDto.TotalCounts.builder()
                    .hardBrake(0)
                    .rapidAccel(0)
                    .laneChange(0)
                    .build();
        } catch (Exception e) {
            log.error("Error getting total counts for reportId: {}", reportId, e);
            return BehaviorAnalysisResponseDto.TotalCounts.builder()
                    .hardBrake(0)
                    .rapidAccel(0)
                    .laneChange(0)
                    .build();
        }
    }

    private BehaviorAnalysisResponseDto.DrivingPattern getDrivingPatternFromDB(Long reportId) {
        try {
            List<EventPatternProjectionDto> eventPatterns = behaviorPatternRepository.findEventPatternsByReportId(reportId);
            log.info("EventPatterns retrieved for reportId {}: {} patterns", reportId, eventPatterns.size());
            
            BehaviorAnalysisResponseDto.DrivingPattern pattern = patternAnalyzer.analyzeDrivingPattern(eventPatterns);
            log.info("DrivingPattern analyzed: {} {}", pattern.getWeekday(), pattern.getTimeslot());
            return pattern;
        } catch (Exception e) {
            log.error("Error getting driving pattern for reportId: {}", reportId, e);
            return createDefaultDrivingPattern();
        }
    }

    private BehaviorAnalysisResponseDto.Compare getCompareFromDB(Long reportId, BehaviorAnalysisResponseDto.TotalCounts totalCounts) {
        try {
            BehaviorAnalysisResponseDto.Compare compare = compareAnalyzer.analyzeCompare(reportId, totalCounts);
            log.info("Compare analysis completed for reportId {}: {}%", reportId, compare.getIncdec());
            return compare;
        } catch (Exception e) {
            log.error("Error getting compare analysis for reportId: {}", reportId, e);
            return createDefaultCompare(totalCounts);
        }
    }

    private BehaviorAnalysisResponseDto createDefaultResponse(String reportId) {
        return BehaviorAnalysisResponseDto.builder()
                .reportId(reportId)
                .totalCounts(BehaviorAnalysisResponseDto.TotalCounts.builder()
                        .hardBrake(0)
                        .rapidAccel(0)
                        .laneChange(0)
                        .build())
                .drivingPattern(createDefaultDrivingPattern())
                .compare(createDefaultCompare(BehaviorAnalysisResponseDto.TotalCounts.builder()
                        .hardBrake(0).rapidAccel(0).laneChange(0).build()))
                .build();
    }

    private BehaviorAnalysisResponseDto.DrivingPattern createDefaultDrivingPattern() {
        return BehaviorAnalysisResponseDto.DrivingPattern.builder()
                .weekday("금요일")
                .timeslot("저녁")
                .build();
    }

    private BehaviorAnalysisResponseDto.Compare createDefaultCompare(BehaviorAnalysisResponseDto.TotalCounts current) {
        // 기본 비교 데이터 (이전 데이터가 없는 경우)
        BehaviorAnalysisResponseDto.TotalCounts previous = BehaviorAnalysisResponseDto.TotalCounts.builder()
                .hardBrake(0)
                .rapidAccel(0)
                .laneChange(0)
                .build();

        double currentTotal = current.getHardBrake() + current.getRapidAccel() + current.getLaneChange();
        double previousTotal = previous.getHardBrake() + previous.getRapidAccel() + previous.getLaneChange();
        
        double incdec = 0.0;
        if (previousTotal > 0) {
            incdec = ((currentTotal - previousTotal) / previousTotal) * 100;
        }
        
        String comment;
        if (incdec > 0) {
            comment = String.format("이전 대비 위험운전 행동이 %.1f%% 증가했습니다.", incdec);
        } else if (incdec < 0) {
            comment = String.format("이전 대비 위험운전 행동이 %.1f%% 감소했습니다.", Math.abs(incdec));
        } else {
            comment = "이전과 동일한 수준의 위험운전 행동을 보입니다.";
        }

        return BehaviorAnalysisResponseDto.Compare.builder()
                .incdec(incdec)
                .comment(comment)
                .chart(BehaviorAnalysisResponseDto.Chart.builder()
                        .hardBrake(BehaviorAnalysisResponseDto.BeforeAfter.builder()
                                .before(previous.getHardBrake())
                                .current(current.getHardBrake())
                                .build())
                        .rapidAccel(BehaviorAnalysisResponseDto.BeforeAfter.builder()
                                .before(previous.getRapidAccel())
                                .current(current.getRapidAccel())
                                .build())
                        .laneChange(BehaviorAnalysisResponseDto.BeforeAfter.builder()
                                .before(previous.getLaneChange())
                                .current(current.getLaneChange())
                                .build())
                        .build())
                .build();
    }

    @Override
    public BehaviorAnalysisResponseDto.TotalCounts calculateTotalCounts(List<String> drivingIds) {
        try {
            BehaviorAnalysisResponseDto.TotalCounts totalCounts = totalCountsRepository.findTotalCountsByDrivingIds(drivingIds);
            return totalCounts != null ? totalCounts : 
                BehaviorAnalysisResponseDto.TotalCounts.builder()
                    .hardBrake(0).rapidAccel(0).laneChange(0).build();
        } catch (Exception e) {
            log.error("Error calculating total counts for drivingIds: {}", drivingIds, e);
            return BehaviorAnalysisResponseDto.TotalCounts.builder()
                .hardBrake(0).rapidAccel(0).laneChange(0).build();
        }
    }

    @Override
    public BehaviorAnalysisResponseDto.DrivingPattern analyzeDrivingPattern(List<String> drivingIds) {
        try {
            List<EventPatternProjectionDto> eventPatterns = behaviorPatternRepository.findEventPatternsByDrivingIds(drivingIds);
            return patternAnalyzer.analyzeDrivingPattern(eventPatterns);
        } catch (Exception e) {
            log.error("Error analyzing driving pattern for drivingIds: {}", drivingIds, e);
            return createDefaultDrivingPattern();
        }
    }

    @Override
    public BehaviorAnalysisResponseDto.Compare compareWithPrevious(String reportId, BehaviorAnalysisResponseDto.TotalCounts currentCounts) {
        try {
            Long reportIdLong = Long.parseLong(reportId);
            return compareAnalyzer.analyzeCompare(reportIdLong, currentCounts);
        } catch (Exception e) {
            log.error("Error comparing with previous for reportId: {}", reportId, e);
            return createDefaultCompare(currentCounts);
        }
    }

    @Override
    public void generateInterimReport(Long reportId, Long userId, List<String> drivingIds) {
        log.info("Generating interim behavior report for reportId: {}, userId: {}, drivingIds: {}", reportId, userId, drivingIds.size());
        // TODO: Implement interim report generation logic
    }

    @Override
    public void generateFinalReport(Long reportId, Long userId, List<String> drivingIds) {
        log.info("Generating final behavior report for reportId: {}, userId: {}, drivingIds: {}", reportId, userId, drivingIds.size());
        // TODO: Implement final report generation logic
    }
}