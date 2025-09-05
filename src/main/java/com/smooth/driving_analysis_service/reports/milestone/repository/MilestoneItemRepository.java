package com.smooth.driving_analysis_service.reports.milestone.repository;

import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneItem;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MilestoneItemRepository extends JpaRepository<MilestoneItem, Long> {

    boolean existsByReportIdAndDrivingId(Long reportId, String drivingId);

    int countByReportId(Long reportId);

    List<MilestoneItem> findByReportIdOrderByOrderNoAsc(Long reportId);
    
    // ===== 새로 추가된 메서드들 =====
    
    // MilestoneReport 엔티티 기반 조회
    List<MilestoneItem> findByReportOrderByOrderNo(MilestoneReport report);
    
    // 특정 리포트의 drivingId 중복 체크
    boolean existsByReportAndDrivingId(MilestoneReport report, String drivingId);
}
