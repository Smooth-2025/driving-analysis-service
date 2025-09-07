package com.smooth.driving_analysis_service.reports.dna.repository;

import com.smooth.driving_analysis_service.reports.dna.entity.DnaSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DnaSnapshotRepository extends JpaRepository<DnaSnapshot, Long> {
    Optional<DnaSnapshot> findByReportId(Long reportId);
    Optional<DnaSnapshot> findByReportIdAndStatus(Long reportId, DnaSnapshot.Status status);
}