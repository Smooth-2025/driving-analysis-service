package com.smooth.driving_analysis_service.reports.milestone.repository;

import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MilestoneItemRepository extends JpaRepository<MilestoneItem, Long> {
    
    List<MilestoneItem> findByReportIdOrderByOrderNo(Long reportId);
    
    @Query("SELECT mi.drivingId FROM MilestoneItem mi WHERE mi.reportId = :reportId ORDER BY mi.orderNo")
    List<String> findDrivingIdsByReportId(@Param("reportId") Long reportId);
    
    int countByReportId(Long reportId);
}