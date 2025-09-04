// repository/DnaSnapshotRepository.java
package com.smooth.driving_analysis_service.reports.dna.repository;

import com.smooth.driving_analysis_service.reports.dna.entity.DnaSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DnaSnapshotRepository extends JpaRepository<DnaSnapshot, Long> {
    Optional<DnaSnapshot> findByReportId(Long reportId);
}
