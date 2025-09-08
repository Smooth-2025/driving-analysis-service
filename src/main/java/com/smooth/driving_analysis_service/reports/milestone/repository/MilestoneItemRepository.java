package com.smooth.driving_analysis_service.reports.milestone.repository;

import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MilestoneItemRepository extends JpaRepository<MilestoneItem, Long> {

    boolean existsByReportIdAndDrivingId(Long reportId, String drivingId);

    int countByReportId(Long reportId);

    List<MilestoneItem> findByReportIdOrderByOrderNoAsc(Long reportId);
    List<MilestoneItem> findAllByReportIdOrderByOrderNoAsc(Long reportId);

    @Query("SELECT mi.drivingId FROM MilestoneItem mi WHERE mi.reportId = :reportId ORDER BY mi.orderNo ASC")
    List<String> findDrivingIdsByReportId(@Param("reportId") Long reportId);
}

