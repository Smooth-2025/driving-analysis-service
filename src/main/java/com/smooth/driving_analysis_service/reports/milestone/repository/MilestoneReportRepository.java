package com.smooth.driving_analysis_service.reports.milestone.repository;

import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MilestoneReportRepository extends JpaRepository<MilestoneReport, Long> {

    // 현재 수집 중인 보고서(헤더) 가져오기
    Optional<MilestoneReport> findFirstByUserIdAndStatusOrderByIdDesc(
            Long userId, MilestoneReport.Status status
    );

    // 사이클 번호 채번용: 해당 사용자 최신 사이클
    Optional<MilestoneReport> findTopByUserIdOrderByCycleNoDesc(Long userId);
}
