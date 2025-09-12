package com.smooth.driving_analysis_service.reports.behavior.repository;

import com.smooth.driving_analysis_service.reports.behavior.entity.BehaviorSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface BehaviorSnapshotRepository extends JpaRepository<BehaviorSnapshot, Long> {
    
    Optional<BehaviorSnapshot> findByReportFkAndStatus(Long reportFk, BehaviorSnapshot.Status status);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BehaviorSnapshot b WHERE b.reportFk = :reportFk AND b.status = :status")
    Optional<BehaviorSnapshot> findByReportFkAndStatusWithLock(@Param("reportFk") Long reportFk, 
                                                               @Param("status") BehaviorSnapshot.Status status);
    
    void deleteByReportFkAndStatus(Long reportFk, BehaviorSnapshot.Status status);
}