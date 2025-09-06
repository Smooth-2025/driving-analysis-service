package com.smooth.driving_analysis_service.reports.milestone.repository;

import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MilestoneItemRepository extends JpaRepository<MilestoneItem, Long> {

    boolean existsByReportIdAndDrivingId(Long reportId, String drivingId);

    int countByReportId(Long reportId);

    List<MilestoneItem> findByReportIdOrderByOrderNoAsc(Long reportId);
}
