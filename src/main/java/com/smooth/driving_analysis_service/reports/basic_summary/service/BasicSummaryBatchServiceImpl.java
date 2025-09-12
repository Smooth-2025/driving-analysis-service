package com.smooth.driving_analysis_service.reports.basic_summary.service;

import com.smooth.driving_analysis_service.reports.batch.dto.SnapshotType;
import com.smooth.driving_analysis_service.reports.basic_summary.entity.BasicSummarySnapshot;
import com.smooth.driving_analysis_service.reports.basic_summary.repository.BasicSummarySnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicSummaryBatchServiceImpl implements BasicSummaryBatchService {
    
    private final BasicSummarySnapshotRepository snapshotRepository;
    
    @Override
    @Transactional
    public void materializeByDrivingIds(String reportId, Long userId, List<String> drivingIds) {
        log.info("Materializing BasicSummary snapshot: reportId={}, userId={}, drivingCount={}", 
                reportId, userId, drivingIds.size());
        
        try {
            // DrivingAccumulatedStats에서 통계 집계
            Map<String, Object> stats = calculateBasicStats(drivingIds);
            
            // 스냅샷 저장 (INTERIM: 업서트, FINAL: 신규)
            saveBasicSummarySnapshot(reportId, userId, stats);
            
            log.info("BasicSummary snapshot materialized successfully: {}", reportId);
            
        } catch (Exception e) {
            log.error("Failed to materialize BasicSummary snapshot: {}", reportId, e);
            throw e;
        }
    }
    
    private Map<String, Object> calculateBasicStats(List<String> drivingIds) {
        log.info("Calculating basic stats for {} driving records", drivingIds.size());
        
        // RDS에서 직접 계산 (DrivingAccumulatedStats 테이블 사용)
        // TODO: DrivingAccumulatedStatsRepository 구현 후 실제 쿼리로 교체
        
        return Map.of(
            "totalDistanceKm", drivingIds.size() * 25.5, // 임시값
            "totalDrivingTimeMinutes", drivingIds.size() * 45,
            "averageSpeedKmh", 42.3,
            "maxSpeedKmh", 78.9,
            "drivingCount", drivingIds.size()
        );
    }
    
    private void saveBasicSummarySnapshot(String reportId, Long userId, Map<String, Object> stats) {
        boolean isInterim = reportId.contains("_interim");
        SnapshotType snapshotType = isInterim ? SnapshotType.INTERIM : SnapshotType.FINAL;
        
        log.info("Saving BasicSummary snapshot: reportId={}, type={}", reportId, snapshotType);
        
        if (isInterim) {
            // INTERIM: 기존 삭제 후 새로 저장 (업서트)
            snapshotRepository.deleteByReportIdAndSnapshotType(Long.parseLong(reportId), SnapshotType.INTERIM);
        }
        
        BasicSummarySnapshot snapshot = BasicSummarySnapshot.builder()
                .reportId(Long.parseLong(reportId))
                .snapshotType(snapshotType)
                .totalDistanceKm((Double) stats.get("totalDistanceKm"))
                .totalDrivingTimeMinutes((Integer) stats.get("totalDrivingTimeMinutes"))
                .averageSpeedKmh((Double) stats.get("averageSpeedKmh"))
                .maxSpeedKmh((Double) stats.get("maxSpeedKmh"))
                .drivingCount((Integer) stats.get("drivingCount"))
                .build();
        
        snapshotRepository.save(snapshot);
    }
}