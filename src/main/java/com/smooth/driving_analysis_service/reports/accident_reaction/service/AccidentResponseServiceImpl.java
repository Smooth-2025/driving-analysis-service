// reports/accident_reaction/service/AccidentResponseServiceImpl.java
package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccidentResponseServiceImpl implements AccidentResponseService {

    @PersistenceContext
    private final EntityManager em;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> buildAccidentResponse(Long reportId) {

        // 1) 스냅샷에서 "내 주행" 집계값 우선 읽기 (FINAL 우선, 없으면 INTERIM)
        Object[] snap = (Object[]) em.createNativeQuery("""
            SELECT total_alerts, reacted_alerts, reaction_rate, avg_reaction_ms, decel_rate, evasive_rate
              FROM v2x_response_score
             WHERE report_id=:rid AND snapshot_type='FINAL'
            UNION ALL
            SELECT total_alerts, reacted_alerts, reaction_rate, avg_reaction_ms, decel_rate, evasive_rate
              FROM v2x_response_score
             WHERE report_id=:rid AND snapshot_type='INTERIM'
             LIMIT 1
        """).setParameter("rid", reportId).getSingleResult();

        int totalAlerts   = ((Number) snap[0]).intValue();
        int reactedAlerts = ((Number) snap[1]).intValue();
        double reactionRate = snap[2]==null?0:((Number) snap[2]).doubleValue();
        double avgReactionMs= snap[3]==null?0:((Number) snap[3]).doubleValue();
        double decelRate    = snap[4]==null?0:((Number) snap[4]).doubleValue();
        double evasiveRate  = snap[5]==null?0:((Number) snap[5]).doubleValue();

        int avgReactionSec = (int) Math.round(avgReactionMs / 1000.0);
        double brakeOrStopRatio = decelRate;
        double riskZoneRatio    = Math.min(1.0, decelRate + evasiveRate); // 감속/정지 + 회피 = 위험 대응 비율

        // 2) 이 리포트에 해당하는 alert 기간 찾기 (코호트 평균 계산용 기간)
        Object[] range = (Object[]) em.createNativeQuery("""
            SELECT MIN(arm.rendered_at) AS from_ts, MAX(arm.rendered_at) AS to_ts
              FROM milestone_item mi
              JOIN accident_reaction_metric arm ON arm.driving_id = mi.driving_id
             WHERE mi.report_id = :rid
        """).setParameter("rid", reportId).getSingleResult();

        Timestamp fromTs = (Timestamp) range[0];
        Timestamp toTs   = (Timestamp) range[1];

        // 3) 같은 기간 전체 사용자 평균(반응한 알림만)
        Number cohortAvgMsNum;
        if (fromTs != null && toTs != null) {
            cohortAvgMsNum = (Number) em.createNativeQuery("""
                SELECT COALESCE(AVG(CASE WHEN reacted=1 THEN reaction_ms END), 0)
                  FROM accident_reaction_metric
                 WHERE rendered_at BETWEEN :from AND :to
            """).setParameter("from", fromTs)
                    .setParameter("to", toTs)
                    .getSingleResult();
        } else {
            // 리포트 기간에 알림이 하나도 없을 때: 전체 기준으로 fallback
            cohortAvgMsNum = (Number) em.createNativeQuery("""
                SELECT COALESCE(AVG(CASE WHEN reacted=1 THEN reaction_ms END), 0)
                  FROM accident_reaction_metric
            """).getSingleResult();
        }

        int cohortAvgSec = (int) Math.round(cohortAvgMsNum.doubleValue() / 1000.0);
        int deltaSec     = avgReactionSec - cohortAvgSec; // 음수=더 빠름

        // 4) 최종 payload
        Map<String, Object> benchmark = Map.of(
                "avgReactionSecOfAllUsers", cohortAvgSec,
                "deltaSec", deltaSec
        );
        Map<String, Object> chart = Map.of(
                "labels", new String[]{"일반 운전자", "내 주행"},
                "valuesSec", new int[]{cohortAvgSec, avgReactionSec}
        );

        Map<String, Object> data = new HashMap<>();
        data.put("receivedAlertCount", totalAlerts);
        data.put("avgReactionSec", avgReactionSec);
        data.put("brakeOrStopRatio", brakeOrStopRatio);
        data.put("riskZoneRatio", riskZoneRatio);
        data.put("benchmark", benchmark);
        data.put("chart", chart);

        // ApiResponse.success(...) 래핑용
        return data;
    }
}
