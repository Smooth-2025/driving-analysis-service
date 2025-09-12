package com.smooth.driving_analysis_service.reports.basic_summary.repository;

import com.smooth.driving_analysis_service.reports.basic_summary.entity.BasicSummarySnapshot;
import com.smooth.driving_analysis_service.reports.batch.dto.SnapshotType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BasicSummarySnapshotRepository extends JpaRepository<BasicSummarySnapshot, Long> {
    
    /**
     * 리포트 ID와 스냅샷 타입으로 조회
     */
    Optional<BasicSummarySnapshot> findByReportIdAndSnapshotType(Long reportId, SnapshotType snapshotType);
    
    /**
     * 리포트 ID와 스냅샷 타입으로 존재 여부 확인
     */
    boolean existsByReportIdAndSnapshotType(Long reportId, SnapshotType snapshotType);
    
    /**
     * 리포트 ID와 스냅샷 타입으로 삭제 (INTERIM 덮어쓰기용)
     */
    void deleteByReportIdAndSnapshotType(Long reportId, SnapshotType snapshotType);
    
    /**
     * 조회 우선순위: FINAL → INTERIM
     */
    default Optional<BasicSummarySnapshot> findByReportIdWithPriority(Long reportId) {
        // 1. FINAL 조회 시도
        Optional<BasicSummarySnapshot> finalSnapshot = findByReportIdAndSnapshotType(reportId, SnapshotType.FINAL);
        if (finalSnapshot.isPresent()) {
            return finalSnapshot;
        }
        
        // 2. INTERIM 조회 시도
        return findByReportIdAndSnapshotType(reportId, SnapshotType.INTERIM);
    }
}