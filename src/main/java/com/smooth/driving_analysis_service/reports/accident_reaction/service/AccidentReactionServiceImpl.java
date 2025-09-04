// reports/accident_reaction/service/AccidentReactionServiceImpl.java
package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import com.smooth.driving_analysis_service.reports.accident_reaction.resolver.DrivingResolver;
import com.smooth.driving_analysis_service.reports.accident_reaction.repository.AccidentReactionMetricRepository;
import lombok.RequiredArgsConstructor; 
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async; 
import org.springframework.stereotype.Service; 
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId; 
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Slf4j @Service @RequiredArgsConstructor
public class AccidentReactionServiceImpl implements AccidentReactionService {
    private final DrivingResolver resolver;
    private final AccidentReactionMetricRepository repo;
    private final AccidentReactionWindowAnalyzer analyzer;

    @Override @Transactional
    public Ack recordAndAnalyzeAsync(String alertId, Long userId, long renderedAtMs, String type) {
        String drivingId = resolver.resolveDrivingId(userId, renderedAtMs, 300);
        
        // 밀리초를 LocalDateTime으로 변환
        LocalDateTime renderedAt = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(renderedAtMs), ZoneId.of("Asia/Seoul"));
        
        repo.upsertAlertRender(alertId, userId, drivingId, renderedAt, type);
        analyzeAsync(alertId, userId, renderedAtMs, drivingId); // 비동기
        return new Ack(drivingId);
    }

    @Async
    protected void analyzeAsync(String alertId, Long userId, long renderedAtMs, String drivingId) {
        try {
            var res = analyzer.findFirstReactionSessionBound(userId, renderedAtMs, drivingId);
            
            LocalDateTime renderedAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(renderedAtMs), ZoneId.of("Asia/Seoul"));
            
            repo.upsertReactionMetric(alertId, userId, drivingId, renderedAt,
                    res.responded(), res.reactionMs(), res.eventType(),
                    res.decelOrStop(), res.evasiveManeuver());
        } catch (Exception e) {
            log.error("accident_reaction analyzeAsync error alertId={}", alertId, e);
        }
    }

    @Override @Transactional(readOnly = true)
    public Map<String,Object> summary(Long userId, String from, String to) {
        var tz = ZoneId.of("Asia/Seoul");
        var fromTs = LocalDate.parse(from).atStartOfDay(tz);
        var toTs   = LocalDate.parse(to).plusDays(1).atStartOfDay(tz).minusNanos(1);
        
        Map<String, Object> stats = repo.getSummaryStats(userId, fromTs.toLocalDateTime(), toTs.toLocalDateTime());
        
        // 결과 가공
        Map<String, Object> result = new HashMap<>();
        result.put("totalAlerts", stats.get("totalAlerts"));
        result.put("avgReactionMs", stats.get("avgReactionMs"));
        result.put("responseRate", calculateResponseRate(stats));
        result.put("reactionTypes", Map.of(
            "decelOrStop", stats.get("decelCount"),
            "evasiveManeuver", stats.get("evasiveCount")
        ));
        
        return result;
    }
    
    private double calculateResponseRate(Map<String, Object> stats) {
        Long total = (Long) stats.get("totalAlerts");
        Long responded = (Long) stats.get("respondedCount");
        
        if (total == null || total == 0) return 0.0;
        if (responded == null) return 0.0;
        
        return (responded.doubleValue() / total.doubleValue()) * 100.0;
    }
}
