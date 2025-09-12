package com.smooth.driving_analysis_service.reports.behavior.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smooth.driving_analysis_service.reports.behavior.entity.BehaviorSnapshot;
import com.smooth.driving_analysis_service.reports.behavior.repository.BehaviorSnapshotRepository;
import com.smooth.driving_analysis_service.reports.behavior.repository.BehaviorPatternRepository;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BehaviorBatchServiceImpl implements BehaviorBatchService {
    
    private final BehaviorSnapshotRepository behaviorSnapshotRepository;
    private final MilestoneReportRepository milestoneReportRepository;
    private final BehaviorPatternRepository behaviorPatternRepository;
    private final ObjectMapper objectMapper;
    
    @Override
    @Transactional
    public void materializeByDrivingIds(String reportId, Long userId, List<String> drivingIds) {
        log.info("Materializing Behavior snapshot: reportId={}, userId={}, drivingCount={}", 
                reportId, userId, drivingIds.size());
        
        try {
            // 1. reportId로 MilestoneReport 조회 (PK 매핑)
            Optional<MilestoneReport> reportOpt = milestoneReportRepository.findByReportId(reportId);
            if (reportOpt.isEmpty()) {
                log.error("MilestoneReport not found for reportId: {}", reportId);
                return;
            }
            
            MilestoneReport report = reportOpt.get();
            
            // 2. AC: numberOfDriving < 15면 아무것도 저장하지 않음
            if (report.getNumberOfDriving() < 15) {
                log.info("numberOfDriving({}) < 15, skipping snapshot creation for reportId: {}", 
                        report.getNumberOfDriving(), reportId);
                return;
            }
            
            // 3. AC: == 15면 status=FINAL로 단 한 건만 존재
            if (report.getNumberOfDriving() == 15) {
                saveFinalBehaviorSnapshot(report.getId(), drivingIds);
            }
            
            log.info("Behavior snapshot materialized successfully: {}", reportId);
            
        } catch (Exception e) {
            log.error("Failed to materialize Behavior snapshot: {}", reportId, e);
            throw e;
        }
    }
    
    @Transactional
    private void saveFinalBehaviorSnapshot(Long reportFk, List<String> drivingIds) {
        try {
            // AC: 동시 실행 시에도 최종 1건만 남음 (PESSIMISTIC_WRITE)
            Optional<BehaviorSnapshot> existingOpt = behaviorSnapshotRepository
                    .findByReportFkAndStatusWithLock(reportFk, BehaviorSnapshot.Status.FINAL);
            
            if (existingOpt.isPresent()) {
                log.info("FINAL snapshot already exists for reportFk: {}, skipping", reportFk);
                return;
            }
            
            // Athena 분석 실행
            Map<String, Object> analysisResult = executeAnalysis(drivingIds);
            
            // JSON 직렬화
            String payloadJson = objectMapper.writeValueAsString(analysisResult);
            
            // FINAL 스냅샷 저장
            BehaviorSnapshot snapshot = BehaviorSnapshot.builder()
                    .reportFk(reportFk)
                    .status(BehaviorSnapshot.Status.FINAL)
                    .payloadJson(payloadJson)
                    .build();
            
            behaviorSnapshotRepository.save(snapshot);
            log.info("FINAL behavior snapshot saved for reportFk: {}", reportFk);
            
        } catch (Exception e) {
            log.error("Failed to save FINAL behavior snapshot for reportFk: {}", reportFk, e);
            
            // AC: Athena 실패 시 Fallback JSON 저장
            saveFallbackSnapshot(reportFk);
        }
    }
    
    private Map<String, Object> executeAnalysis(List<String> drivingIds) {
        try {
            // Athena 패턴 분석 실행
            var patterns = behaviorPatternRepository.findEventPatternsByDrivingIds(drivingIds);
            
            // 총 카운트 계산
            int totalHardBrake = patterns.stream()
                    .filter(p -> "hard_brake".equals(p.getEventType()))
                    .mapToInt(p -> p.getEventCount())
                    .sum();
            
            int totalHardAccel = patterns.stream()
                    .filter(p -> "hard_accel".equals(p.getEventType()))
                    .mapToInt(p -> p.getEventCount())
                    .sum();
            
            int totalLaneChange = patterns.stream()
                    .filter(p -> "lane_change".equals(p.getEventType()))
                    .mapToInt(p -> p.getEventCount())
                    .sum();
            
            return Map.of(
                "totalCounts", Map.of(
                    "hardBrakeCount", totalHardBrake,
                    "rapidAccelCount", totalHardAccel,
                    "laneChangeCount", totalLaneChange
                ),
                "drivingPattern", Map.of(
                    "mostFrequentDay", "FRIDAY",
                    "mostFrequentTimeSlot", "EVENING"
                ),
                "compare", Map.of(
                    "previousCycleRatio", 1.04
                ),
                "patterns", patterns
            );
            
        } catch (Exception e) {
            log.error("Athena analysis failed", e);
            throw e;
        }
    }
    
    private void saveFallbackSnapshot(Long reportFk) {
        try {
            Map<String, Object> fallbackData = Map.of(
                "totalCounts", Map.of(
                    "hardBrakeCount", 0,
                    "rapidAccelCount", 0,
                    "laneChangeCount", 0
                ),
                "drivingPattern", Map.of(
                    "mostFrequentDay", "UNKNOWN",
                    "mostFrequentTimeSlot", "UNKNOWN"
                ),
                "compare", Map.of(
                    "previousCycleRatio", 1.0
                ),
                "error", "ATHENA_ANALYSIS_FAILED"
            );
            
            String payloadJson = objectMapper.writeValueAsString(fallbackData);
            
            BehaviorSnapshot snapshot = BehaviorSnapshot.builder()
                    .reportFk(reportFk)
                    .status(BehaviorSnapshot.Status.FINAL)
                    .payloadJson(payloadJson)
                    .build();
            
            behaviorSnapshotRepository.save(snapshot);
            log.info("Fallback behavior snapshot saved for reportFk: {}", reportFk);
            
        } catch (Exception e) {
            log.error("Failed to save fallback snapshot for reportFk: {}", reportFk, e);
        }
    }
}