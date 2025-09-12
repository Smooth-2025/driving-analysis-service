package com.smooth.driving_analysis_service.reports.dna.repository;

import com.smooth.driving_analysis_service.reports.dna.entity.DnaSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DnaSnapshotRepository extends JpaRepository<DnaSnapshot, Long> {

    /**
     * 리포트 ID로 DNA 스냅샷 조회 (레거시 - Long)
     */
    Optional<DnaSnapshot> findByReportId(Long reportId);
    
    /**
     * 리포트 ID로 DNA 스냅샷 조회 (새로운 - String)
     */
    Optional<DnaSnapshot> findByReportId(String reportId);

    /**
     * 사용자 ID로 최신 DNA 스냅샷 조회
     */
    @Query("SELECT d FROM DnaSnapshot d WHERE d.userId = :userId ORDER BY d.id DESC LIMIT 1")
    Optional<DnaSnapshot> findLatestByUserId(@Param("userId") Long userId);

    /**
     * 사용자 ID와 상태로 DNA 스냅샷 조회
     */
    Optional<DnaSnapshot> findByUserIdAndStatus(Long userId, DnaSnapshot.Status status);

    /**
     * 리포트 ID와 상태로 DNA 스냅샷 조회
     */
    Optional<DnaSnapshot> findByReportIdAndStatus(Long reportId, DnaSnapshot.Status status);
    
    /**
     * 사용자 ID, 리포트 ID, 상태로 DNA 스냅샷 조회
     */
    Optional<DnaSnapshot> findByUserIdAndReportIdAndStatus(Long userId, Long reportId, DnaSnapshot.Status status);
}