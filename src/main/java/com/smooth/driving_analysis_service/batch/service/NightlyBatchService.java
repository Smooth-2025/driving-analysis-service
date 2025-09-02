package com.smooth.driving_analysis_service.batch.service;

import com.smooth.driving_analysis_service.batch.athena.AthenaStagePort;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneItem;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport.Status;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneItemRepository;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NightlyBatchService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final MilestoneReportRepository milestoneReportRepository;
    private final MilestoneItemRepository milestoneItemRepository;
    private final JdbcTemplate jdbc;
    private final BatchWatermarkService watermark;
    private final AthenaStagePort athenaStage;

    /** 운영 스케줄러 진입: 워터마크 존중 */
    @Transactional
    public int runOperational(LocalDate asOf) {
        if (watermark.alreadyProcessed(asOf)) {
            log.info("[Nightly] Skip (already processed): {}", asOf);
            return 0;
        }
        int processed = runCore(asOf, false);
        watermark.markDone(asOf);
        return processed;
    }

    /** 수동 실행: force=true면 워터마크 무시 */
    @Transactional
    public int runManual(LocalDate asOf, boolean force) {
        if (!force && watermark.alreadyProcessed(asOf)) {
            log.info("[Manual] Skip (already processed): {}", asOf);
            return 0;
        }
        int processed = runCore(asOf, force);
        watermark.markDone(asOf);
        return processed;
    }

    /** 수동 연속 실행 */
    @Transactional
    public int runRangeManual(LocalDate start, int days, boolean force) {
        int total = 0;
        for (int i = 0; i < days; i++) {
            total += runManual(start.plusDays(i), force);
        }
        return total;
    }

    /** 공통 코어: COLLECTING=스냅샷, PROCESSING=풀집계 → COMPLETED */
    private int runCore(LocalDate asOf, boolean force) {
        // 예: 09:15 KST까지 들어온 데이터 기준으로 집계
        Instant cutoff = LocalDateTime.of(asOf, LocalTime.of(9, 15)).atZone(KST).toInstant();

        List<MilestoneReport> targets = milestoneReportRepository
                .findAllByStatusInOrderByCreatedAtAsc(EnumSet.of(Status.COLLECTING, Status.PROCESSING));

        log.info("[Nightly] targets={} asOf={} cutoff={}", targets.size(), asOf, cutoff);

        int success = 0;
        for (MilestoneReport m : targets) {
            try {
                processOneMilestone(m, cutoff, force);
                success++;
            } catch (Exception e) {
                log.error("[Nightly] milestone {} failed: {}", m.getId(), e.getMessage(), e);
            }
        }
        return success;
    }

    @Transactional
    protected void processOneMilestone(MilestoneReport m, Instant cutoff, boolean force) {
        Long reportId = m.getId();
        Long userId   = m.getUserId();

        // Athena 뷰/파티션 반영(지연 보정 등)
        athenaStage.refreshForUserUntil(userId, cutoff);

        List<String> drivingIds = milestoneItemRepository
                .findAllByReportIdOrderByOrderNoAsc(reportId)
                .stream().map(MilestoneItem::getDrivingId).toList();

        if (drivingIds.isEmpty()) {
            log.warn("[Nightly] report {} has no items; skip", reportId);
            if (m.getStatus() == Status.PROCESSING) {
                m.setStatus(Status.COMPLETED);
                m.setUpdatedAt(LocalDateTime.now());
                milestoneReportRepository.save(m);
            }
            return;
        }

        // 공통: 요약 스냅샷 (15회 총합)
        snapshotReportMetrics(reportId, userId, drivingIds);

        if (m.getStatus() == Status.COLLECTING && !force) {
            log.info("[Nightly] report {} (COLLECTING) snapshot updated", reportId);
            return;
        }

        // PROCESSING(또는 force) → 이벤트 합계 재적재
        deleteEventAgg(drivingIds);
        insertEventAgg(drivingIds);

        // driving_time_bin 제거에 따라 시간대 bin 재계산은 더 이상 수행하지 않음

        m.setStatus(Status.COMPLETED);
        m.setUpdatedAt(LocalDateTime.now());
        milestoneReportRepository.save(m);

        log.info("[Nightly] report {} fully aggregated & completed", reportId);
    }

    /* ====== 집계 SQLs ====== */
    private void deleteEventAgg(List<String> drivingIds) {
        if (drivingIds == null || drivingIds.isEmpty()) return;
        jdbc.update(
                "DELETE FROM driving_event_agg WHERE driving_id IN (" + placeholders(drivingIds.size()) + ")",
                drivingIds.toArray()
        );
    }

    private void insertEventAgg(List<String> drivingIds) {
        if (drivingIds == null || drivingIds.isEmpty()) return;
        String sql = """
            INSERT INTO driving_event_agg
              (driving_id, user_id, lane_change_count, hard_brake_count, rapid_accel_count, sharp_turn_count)
            SELECT dr.driving_id, dr.user_id, dr.lane_change_count, dr.hard_brake_count, dr.rapid_accel_count, dr.sharp_turn_count
            FROM driving_record dr
            WHERE dr.driving_id IN (""" + placeholders(drivingIds.size()) + ")";
        jdbc.update(sql, drivingIds.toArray());
    }

    private void snapshotReportMetrics(Long reportId, Long userId, List<String> drivingIds) {
        jdbc.update("DELETE FROM driving_analysis.report_metrics WHERE milestone_id=?", reportId);

        List<Object> params = new ArrayList<>();
        params.add(reportId);
        params.addAll(drivingIds);

        String sql = """
            INSERT INTO report_metrics (milestone_id, user_id, total_minutes, total_distance, trips, created_at)
            SELECT ?, dr.user_id,
                   COALESCE(SUM(dr.driving_minutes),0),
                   COALESCE(SUM(dr.total_distance),0),
                   COUNT(*),
                   NOW(6)
            FROM driving_record dr
            WHERE dr.driving_id IN (""" + placeholders(drivingIds.size()) + ") GROUP BY dr.user_id";
        jdbc.update(sql, params.toArray());
    }

    private String placeholders(int n) {
        return String.join(",", java.util.Collections.nCopies(n, "?"));
    }
}
