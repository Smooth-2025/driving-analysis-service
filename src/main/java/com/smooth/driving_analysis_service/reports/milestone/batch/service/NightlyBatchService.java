package com.smooth.driving_analysis_service.reports.milestone.batch.service;

import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneItem;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport.Status;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneItemRepository;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import com.smooth.driving_analysis_service.reports.milestone.repository.ReportMetricsRepository;
import com.smooth.driving_analysis_service.driving.service.AthenaQueryService; // (기존 서비스 재사용)
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

    private final MilestoneReportRepository milestoneReportRepository;
    private final MilestoneItemRepository milestoneItemRepository;
    private final ReportMetricsRepository reportMetricsRepository;
    private final JdbcTemplate jdbc;
    private final AthenaQueryService athena; // Athena 컷오프/재시도 훅에서 사용

    /** 09:30 배치 진입점: COLLECTING + PROCESSING 모두 선택 */
    @Transactional
    public int runNightly(LocalDate runDateKst) {
        // KST 기준 컷오프 (예: 09:15 이전 데이터까지만 집계)
        ZoneId KST = ZoneId.of("Asia/Seoul");
        LocalDateTime cutoffKst = LocalDateTime.of(runDateKst, LocalTime.of(9, 15));
        Instant cutoff = cutoffKst.atZone(KST).toInstant();

        List<MilestoneReport> targets = milestoneReportRepository
                .findAllByStatusInOrderByCreatedAtAsc(EnumSet.of(Status.COLLECTING, Status.PROCESSING));

        log.info("[Nightly] targets={} cutoff={}", targets.size(), cutoff);

        int success = 0;
        for (MilestoneReport m : targets) {
            try {
                processOneMilestone(m, cutoff);
                success++;
            } catch (Exception e) {
                log.error("[Nightly] milestone {} failed: {}", m.getId(), e.getMessage(), e);
            }
        }
        return success;
    }

    /** 리포트 단위 처리: COLLECTING=스냅샷만, PROCESSING=풀계산 후 COMPLETED */
    @Transactional
    public void processOneMilestone(MilestoneReport m, Instant cutoff) {
        Long reportId = m.getId();
        Long userId   = m.getUserId();

        // Athena 사전 단계: 컷오프 기준으로 최신 원본 반영(지연 대비 재시도 포함)
        runAthenaStageWithRetry(userId, cutoff, 3, Duration.ofMinutes(2));

        List<String> drivingIds = milestoneItemRepository
                .findAllByReportIdOrderByOrderNoAsc(reportId)
                .stream().map(MilestoneItem::getDrivingId).toList();

        if (drivingIds.isEmpty()) {
            log.warn("[Nightly] report {} has no items; skip", reportId);
            if (m.getStatus() == Status.PROCESSING) {
                // 아이템이 없더라도 상태는 완료로 넘길지 정책에 따라 선택
                m.setStatus(Status.COMPLETED);
                m.setUpdatedAt(LocalDateTime.now());
                milestoneReportRepository.save(m);
            }
            return;
        }

        // 공통: 스냅샷(요약 메트릭) 갱신
        snapshotReportMetrics(reportId, userId, drivingIds);

        if (m.getStatus() == Status.COLLECTING) {
            // COLLECTING은 여기서 끝 (상태 유지)
            log.info("[Nightly] report {} (COLLECTING) snapshot updated", reportId);
            return;
        }

        // PROCESSING: 풀 집계 재생성 -> 완료 전환
        deleteEventAgg(drivingIds);
        insertEventAgg(drivingIds);
        recomputeTimeBins(drivingIds);

        m.setStatus(Status.COMPLETED);
        m.setUpdatedAt(LocalDateTime.now());
        milestoneReportRepository.save(m);

        log.info("[Nightly] report {} (PROCESSING) fully aggregated & completed", reportId);
    }

    /* =========================
     * Athena 컷오프/재시도 훅
     * ========================= */
    private void runAthenaStageWithRetry(Long userId, Instant cutoff, int maxTry, Duration backoff) {
        for (int i = 1; i <= maxTry; i++) {
            try {
                // 예시: 사용자+컷오프 기준 S3 파티션/뷰 새로고침 or 결과 테이블 머티리얼라이즈
                // 실제 쿼리는 AthenaQueryService에 정의된 메서드 사용
                athenaRefreshViews(userId, cutoff);
                return;
            } catch (Exception e) {
                log.warn("[Nightly][Athena] try {}/{} failed: {}", i, maxTry, e.toString());
                if (i == maxTry) throw e;
                try { Thread.sleep(backoff.toMillis()); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void athenaRefreshViews(Long userId, Instant cutoff) {
        // 여기서는 간단히 훅만 호출. 실제 쿼리는 AthenaQueryService 구현에 작성.
        // 예) athena.refreshUserPartitions(userId, cutoff);
        // 예) athena.materializeUserDaily(userId, LocalDate.ofInstant(cutoff, ZoneId.of("Asia/Seoul")));
        log.debug("[Nightly][Athena] refresh for user={} cutoff={}", userId, cutoff);
        // 필요시 결과 확인/대기 로직도 AthenaQueryService에 캡슐화
    }

    /* =========================
     * 스냅샷/집계 SQL (MySQL)
     * ========================= */
    private void deleteEventAgg(List<String> drivingIds) {
        jdbc.update("DELETE FROM driving_event_agg WHERE driving_id IN (" + placeholders(drivingIds.size()) + ")", drivingIds.toArray());
    }

    private void insertEventAgg(List<String> drivingIds) {
        String sql = """
            INSERT INTO driving_event_agg
              (driving_id, user_id, lane_change_count, hard_brake_count, rapid_accel_count, sharp_turn_count)
            SELECT dr.driving_id, dr.user_id, dr.lane_change_count, dr.hard_brake_count, dr.rapid_accel_count, dr.sharp_turn_count
            FROM driving_record dr
            WHERE dr.driving_id IN (""" + placeholders(drivingIds.size()) + ")";
        jdbc.update(sql, drivingIds.toArray());
    }

    private void recomputeTimeBins(List<String> drivingIds) {
        for (String id : drivingIds) {
            jdbc.update("DELETE FROM driving_time_bin WHERE driving_id=?", id);
            String sql = """
                WITH RECURSIVE minutes AS (
                  SELECT dr.driving_id, dr.user_id, dr.start_time, dr.end_time, 0 AS m
                  FROM driving_record dr WHERE dr.driving_id = ?
                  UNION ALL
                  SELECT driving_id, user_id, start_time, end_time, m+1
                  FROM minutes
                  WHERE TIMESTAMPADD(MINUTE, m+1, start_time) <= end_time
                )
                INSERT INTO driving_time_bin (driving_id, user_id, hour_of_day, minutes)
                SELECT driving_id, user_id,
                       HOUR(TIMESTAMPADD(MINUTE, m, start_time)) AS hour_of_day,
                       COUNT(*) AS minutes
                FROM minutes
                GROUP BY driving_id, user_id, hour_of_day
            """;
            jdbc.update(sql, id);
        }
    }

    private void snapshotReportMetrics(Long reportId, Long userId, List<String> drivingIds) {
        jdbc.update("DELETE FROM report_metrics WHERE milestone_id=?", reportId);
        List<Object> params = new ArrayList<>();
        params.add(reportId);
        params.addAll(drivingIds);

        String sql = """
            INSERT INTO report_metrics (milestone_id, user_id, total_minutes, total_distance, trips, created_at)
            SELECT ?, dr.user_id, COALESCE(SUM(dr.driving_minutes),0), COALESCE(SUM(dr.total_distance),0), COUNT(*), NOW(6)
            FROM driving_record dr
            WHERE dr.driving_id IN (""" + placeholders(drivingIds.size()) + ") GROUP BY dr.user_id";
        jdbc.update(sql, params.toArray());
    }

    private String placeholders(int n) {
        return String.join(",", java.util.Collections.nCopies(n, "?"));
    }
}
