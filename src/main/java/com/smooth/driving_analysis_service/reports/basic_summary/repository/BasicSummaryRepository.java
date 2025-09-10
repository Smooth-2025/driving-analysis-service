package com.smooth.driving_analysis_service.reports.basic_summary.repository;

import com.smooth.driving_analysis_service.reports.basic_summary.entity.BasicSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface BasicSummaryRepository extends JpaRepository<BasicSummary, Long> {
    
    @Query("SELECT bs FROM BasicSummary bs WHERE bs.reportId = :reportId AND bs.snapshotType = 'FINAL'")
    Optional<BasicSummary> findFinalByReportId(@Param("reportId") Long reportId);
    
    @Query("SELECT bs FROM BasicSummary bs WHERE bs.reportId = :reportId AND bs.snapshotType = 'INTERIM'")
    Optional<BasicSummary> findInterimByReportId(@Param("reportId") Long reportId);
    
    Optional<BasicSummary> findByReportIdAndSnapshotType(Long reportId, BasicSummary.SnapshotType snapshotType);
    
    // 사용자별 조회 메서드 추가
    Optional<BasicSummary> findByUserIdAndReportIdAndSnapshotType(Long userId, Long reportId, BasicSummary.SnapshotType snapshotType);
    
    void deleteByReportIdAndSnapshotType(Long reportId, BasicSummary.SnapshotType snapshotType);
    

}