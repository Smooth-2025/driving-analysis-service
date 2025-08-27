package com.smooth.driving_analysis_service.reports.milestone.repository;

import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MilestoneReportRepository extends JpaRepository<MilestoneReport, Long> {
    Optional<MilestoneReport> findByUserIdAndCycleNo(Long userId, Integer cycleNo);
}
