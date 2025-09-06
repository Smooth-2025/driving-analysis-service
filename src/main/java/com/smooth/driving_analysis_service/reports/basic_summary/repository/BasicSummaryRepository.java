package com.smooth.driving_analysis_service.reports.basic_summary.repository;

import com.smooth.driving_analysis_service.reports.basic_summary.entity.BasicSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

<<<<<<< HEAD
import java.math.BigDecimal;
import java.time.LocalDate;
=======
>>>>>>> origin/feat-us7.2
import java.util.Optional;

@Repository
public interface BasicSummaryRepository extends JpaRepository<BasicSummary, Long> {
    
    @Query("SELECT bs FROM BasicSummary bs WHERE bs.reportId = :reportId AND bs.snapshotType = 'FINAL'")
    Optional<BasicSummary> findFinalByReportId(@Param("reportId") Long reportId);
    
    @Query("SELECT bs FROM BasicSummary bs WHERE bs.reportId = :reportId AND bs.snapshotType = 'INTERIM'")
    Optional<BasicSummary> findInterimByReportId(@Param("reportId") Long reportId);
    
<<<<<<< HEAD
    Optional<BasicSummary> findByReportIdAndSnapshotType(Long reportId, BasicSummary.SnapshotType snapshotType);
    
    void deleteByReportIdAndSnapshotType(Long reportId, BasicSummary.SnapshotType snapshotType);
    
    @Query("SELECT das FROM DrivingAccumulatedStats das WHERE das.reportId = :reportId")
    BasicSummaryProjection calculateSummaryByReportId(@Param("reportId") Long reportId);
    
    interface BasicSummaryProjection {
        Long getTotalDrivingCount();
        BigDecimal getTotalDistanceKm();
        Long getTotalDrivingTimeMinutes();
        LocalDate getPeriodStart();
        LocalDate getPeriodEnd();
    }
=======
    void deleteByReportIdAndSnapshotType(Long reportId, BasicSummary.SnapshotType snapshotType);
>>>>>>> origin/feat-us7.2
}