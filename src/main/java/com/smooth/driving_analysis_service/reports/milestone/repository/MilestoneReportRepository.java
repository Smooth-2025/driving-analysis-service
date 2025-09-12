package com.smooth.driving_analysis_service.reports.milestone.repository;

import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MilestoneReportRepository extends JpaRepository<MilestoneReport, Long> {
    
    List<MilestoneReport> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    
    Optional<MilestoneReport> findFirstByUserIdAndStatusOrderByIdDesc(Long userId, MilestoneReport.Status status);
    
    Optional<MilestoneReport> findTopByUserIdOrderByCycleNoDesc(Long userId);
    
    Optional<MilestoneReport> findByReportId(String reportId);
    
    List<MilestoneReport> findAllByStatus(MilestoneReport.Status status);
    
    Page<MilestoneReport> findByUserIdAndStatusInAndUpdatedAtBeforeOrderByUpdatedAtDesc(
            Long userId, List<MilestoneReport.Status> statuses, LocalDateTime before, Pageable pageable);
    
    Page<MilestoneReport> findByUserIdAndStatusInOrderByUpdatedAtDesc(
            Long userId, List<MilestoneReport.Status> statuses, Pageable pageable);
}