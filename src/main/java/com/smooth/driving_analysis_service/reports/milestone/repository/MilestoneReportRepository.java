package com.smooth.driving_analysis_service.reports.milestone.repository;

import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MilestoneReportRepository
                extends org.springframework.data.jpa.repository.JpaRepository<MilestoneReport, Long> {

        // 현재 수집 중인 보고서(헤더) 가져오기 (기존)
        Optional<MilestoneReport> findFirstByUserIdAndStatusOrderByIdDesc(
                        Long userId, MilestoneReport.Status status);

        // 사이클 번호 채번용: 해당 사용자 최신 사이클 (기존)
        Optional<MilestoneReport> findTopByUserIdOrderByCycleNoDesc(Long userId);

        // ===== 타임라인 전용: COLLECTING 제외하고 PROCESSING/COMPLETED만 페이징 조회 =====
        Page<MilestoneReport> findByUserIdAndStatusInOrderByCreatedAtDesc(
                        Long userId, List<MilestoneReport.Status> statuses, Pageable pageable);

        Page<MilestoneReport> findByUserIdAndStatusInAndCreatedAtBeforeOrderByCreatedAtDesc(
                        Long userId, List<MilestoneReport.Status> statuses, LocalDateTime before, Pageable pageable);

        // 읽음 처리용
        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("update MilestoneReport m set m.read = :read where m.id = :id")
        int updateRead(@Param("id") Long id, @Param("read") boolean read);

        List<MilestoneReport> findAllByUserIdOrderByCreatedAtDesc(Long userId);

        // 배치 처리용: 특정 상태의 리포트들 조회
        List<MilestoneReport> findByStatus(MilestoneReport.Status status);

        // ===== 새로 추가된 메서드들 =====

        // reportId로 조회
        @Query("SELECT m FROM MilestoneReport m WHERE m.reportId = :reportId")
        Optional<MilestoneReport> findByReportId(@Param("reportId") String reportId);

        // 사용자별 특정 상태의 리포트 조회
        Optional<MilestoneReport> findByUserIdAndStatus(Long userId, MilestoneReport.Status status);

        // 사용자별 최대 사이클 번호 조회
        @Query("SELECT MAX(m.cycleNo) FROM MilestoneReport m WHERE m.userId = :userId")
        Integer findMaxCycleNoByUserId(@Param("userId") Long userId);

        // 특정 상태이면서 특정 시간 이전에 업데이트된 리포트들 조회 (정리용)
        List<MilestoneReport> findByStatusAndUpdatedAtBefore(MilestoneReport.Status status, LocalDateTime before);
}
