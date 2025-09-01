package com.smooth.driving_analysis_service.reports.milestone.repository;

import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MilestoneItemRepository extends JpaRepository<MilestoneItem, Long> {

    boolean existsByReportIdAndDrivingId(Long reportId, String drivingId);

    int countByReportId(Long reportId);

    List<MilestoneItem> findAllByReportId(Long reportId);

    List<MilestoneItem> findByReportIdOrderByOrderNoAsc(Long reportId);

    @Query("select mi.drivingId from MilestoneItem mi where mi.reportId = :reportId order by mi.orderNo asc")
    List<String> findAllDrivingIdsByReportId(@Param("reportId") Long reportId);

    @Query("select mi from MilestoneItem mi where mi.reportId = :reportId order by mi.orderNo asc")
    List<MilestoneItem> findAllByReportIdOrderByOrderNoAsc(@Param("reportId") Long reportId);

}
