package com.smooth.driving_analysis_service.reports.batch.controller;

import com.smooth.driving_analysis_service.global.auth.AuthenticationUtils;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/driving-analysis/internal/debug/reports")
public class ReportDebugController {

    @PersistenceContext
    private EntityManager em;

    @GetMapping("/{reportId}/summary")
    public Map<String, Object> summary(@PathVariable Long reportId) {
        Long userId = AuthenticationUtils.getCurrentUserIdOrThrow();
        var report = em.find(MilestoneReport.class, reportId);
        Long items = em.createQuery(
                        "select count(i) from MilestoneItem i where i.report.id=:rid", Long.class)
                .setParameter("rid", reportId).getSingleResult();

        long interimMetrics = count("report_metrics", reportId, "INTERIM");
        long interimPattern = count("time_pattern_bins", reportId, "INTERIM");
        long interimEvents  = count("trip_event_agg", reportId, "INTERIM");
        long finalDna       = count("driving_dna", reportId, "FINAL");
        long finalV2x       = count("v2x_response_score", reportId, "FINAL");

        return Map.of(
                "reportId", reportId,
                "status", report != null ? report.getStatus().name() : "N/A",
                "itemCount", items,
                "snapshots", Map.of(
                        "interim", Map.of(
                                "metrics", interimMetrics,
                                "timePattern", interimPattern,
                                "eventAgg", interimEvents
                        ),
                        "final", Map.of(
                                "dna", finalDna,
                                "v2x", finalV2x
                        )
                ),
                "interimReady", items >= 4
        );
    }

    private long count(String table, Long reportId, String type) {
        Long userId = AuthenticationUtils.getCurrentUserIdOrThrow();
        String sql = "select count(*) from " + table +
                " where report_id = :rid and snapshot_type = :type";
        Number n = (Number) em.createNativeQuery(sql)
                .setParameter("rid", reportId)
                .setParameter("type", type)
                .getSingleResult();
        return n.longValue();
    }
}
