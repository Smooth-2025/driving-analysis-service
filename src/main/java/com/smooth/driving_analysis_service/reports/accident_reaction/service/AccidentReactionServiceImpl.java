// reports/accident_reaction/service/AccidentReactionServiceImpl.java
package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import com.smooth.driving_analysis_service.reports.accident_reaction.resolver.DrivingResolver;
import com.smooth.driving_analysis_service.reports.accident_reaction.repository.AccidentReactionMetricRepository;
import com.smooth.driving_analysis_service.reports.accident_reaction.repository.AccidentReactionRepositoryImpl;
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
    private final AccidentReactionRepositoryImpl customRepo;
    private final AccidentReactionWindowAnalyzer analyzer;

    @Transactional
    public String recordAndAnalyzeAsync(String alertId, Long userId, long renderedAtMs, String type) {
        String drivingId = resolver.resolveDrivingId(userId, renderedAtMs, 300);
        
        // 밀리초를 LocalDateTime으로 변환
        LocalDateTime renderedAt = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(renderedAtMs), ZoneId.of("Asia/Seoul"));
        
        customRepo.upsertAlertRender(alertId, userId, drivingId, renderedAt, type);
        analyzeAsync(alertId, userId, renderedAtMs, drivingId); // 비동기
        return drivingId;
    }

    @Async
    protected void analyzeAsync(String alertId, Long userId, long renderedAtMs, String drivingId) {
        try {
            var res = analyzer.findFirstReactionSessionBound(userId, renderedAtMs, drivingId);
            
            LocalDateTime renderedAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(renderedAtMs), ZoneId.of("Asia/Seoul"));
            
            customRepo.upsertReactionMetric(alertId, userId, drivingId, renderedAt,
                    res.responded(), res.reactionMs(), res.eventType(),
                    res.decelOrStop(), res.evasiveManeuver());
        } catch (Exception e) {
            log.error("accident_reaction analyzeAsync error alertId={}", alertId, e);
        }
    }

    @Transactional(readOnly = true)
    public Map<String,Object> summary(Long userId, String from, String to) {
        var tz = ZoneId.of("Asia/Seoul");
        var fromTs = LocalDate.parse(from).atStartOfDay(tz);
        var toTs   = LocalDate.parse(to).plusDays(1).atStartOfDay(tz).minusNanos(1);
        
        Map<String, Object> stats = customRepo.getSummaryStats(userId, fromTs.toLocalDateTime(), toTs.toLocalDateTime());
        
        // 결과 가공
        Map<String, Object> result = new HashMap<>();
        result.put("totalAlerts", stats.get("totalAlerts"));
        result.put("avgReactionMs", stats.get("avgReactionMs"));
        result.put("responseRate", ((Number) stats.get("reactionRate")).doubleValue() * 100.0);
        result.put("reactionTypes", Map.of(
            "decelOrStop", stats.get("decelRate"),
            "evasiveManeuver", stats.get("evasiveRate")
        ));
        
        return result;
    }

    @Override
    public void createOrUpdateInterimSnapshot(Long reportId) {
        // TODO: Implement interim snapshot creation logic
        log.info("Creating interim snapshot for reportId: {}", reportId);
    }

    @Override
    public void createFinalSnapshot(Long reportId) {
        // TODO: Implement final snapshot creation logic
        log.info("Creating final snapshot for reportId: {}", reportId);
    }
    

}
