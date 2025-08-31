package com.smooth.driving_analysis_service.reports.milestone.repository;

import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MilestoneReportRepository extends org.springframework.data.jpa.repository.JpaRepository<MilestoneReport, Long> {

    // 현재 수집 중인 보고서(헤더) 가져오기 (기존)
    Optional<MilestoneReport> findFirstByUserIdAndStatusOrderByIdDesc(
            Long userId, MilestoneReport.Status status
    );

    // 사이클 번호 채번용: 해당 사용자 최신 사이클 (기존)
    Optional<MilestoneReport> findTopByUserIdOrderByCycleNoDesc(Long userId);

    // ===== 타임라인 전용: COLLECTING 제외하고 PROCESSING/COMPLETED만 페이징 조회 =====
    Page<MilestoneReport> findByUserIdAndStatusInOrderByCreatedAtDesc(
            Long userId, List<MilestoneReport.Status> statuses, Pageable pageable
    );

    Page<MilestoneReport> findByUserIdAndStatusInAndCreatedAtBeforeOrderByCreatedAtDesc(
            Long userId, List<MilestoneReport.Status> statuses, LocalDateTime before, Pageable pageable
    );
}
