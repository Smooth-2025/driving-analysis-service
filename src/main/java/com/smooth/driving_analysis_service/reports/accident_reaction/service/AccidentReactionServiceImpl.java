package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import com.smooth.driving_analysis_service.reports.accident_reaction.dto.Ack;
import com.smooth.driving_analysis_service.reports.accident_reaction.resolver.DrivingResolver;
import com.smooth.driving_analysis_service.reports.accident_reaction.repository.AccidentReactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZoneId;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

@Slf4j @Service @RequiredArgsConstructor
public class AccidentReactionServiceImpl implements AccidentReactionService {
    private final DrivingResolver resolver;
    private final AccidentReactionRepository repo;
    private final AccidentReactionWindowAnalyzer analyzer;

    @Override @Transactional
    public Ack recordAndAnalyzeAsync(String alertId, Long userId, long renderedAtMs, String type) {
        String drivingId = resolver.resolveDrivingId(userId, renderedAtMs, 300);
        repo.upsertAlertRender(alertId, userId, drivingId, renderedAtMs, type);
        analyzeAsync(alertId, userId, renderedAtMs, drivingId); // 비동기
        return new Ack(drivingId);
    }

    @Async
    protected void analyzeAsync(String alertId, Long userId, long renderedAtMs, String drivingId) {
        try {
            var res = analyzer.findFirstReactionSessionBound(userId, renderedAtMs, drivingId);
            repo.upsertReactionMetric(alertId, userId, drivingId, renderedAtMs,
                    res.responded(), res.reactionMs(), res.eventType(),
                    res.decelOrStop(), res.evasiveManeuver());
        } catch (Exception e) {
            log.error("accident_reaction analyzeAsync error alertId={}", alertId, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> summary(Long userId, String from, String to) {
        var tz = ZoneId.of("Asia/Seoul");
        var fromTs = LocalDate.parse(from).atStartOfDay(tz);
        var toTs = LocalDate.parse(to).plusDays(1).atStartOfDay(tz).minusNanos(1);
        return repo.summary(userId, fromTs.toLocalDateTime(), toTs.toLocalDateTime());
    }

    @Override
    public Map<String, Object> buildAccidentResponse(Long reportId) {
        // TODO: Implement accident response building logic
        Map<String, Object> response = new HashMap<>();
        response.put("reportId", reportId);
        response.put("status", "processed");
        return response;
    }
}
