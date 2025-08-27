package com.smooth.driving_analysis_service.reports.milestone.repository;

import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MilestoneItemRepository extends JpaRepository<MilestoneItem, Long> {
    long countByReportId(Long reportId);
    boolean existsByReportIdAndDrivingId(Long reportId, String drivingId);
    List<MilestoneItem> findByReportIdOrderByOrderNoAsc(Long reportId);
}
