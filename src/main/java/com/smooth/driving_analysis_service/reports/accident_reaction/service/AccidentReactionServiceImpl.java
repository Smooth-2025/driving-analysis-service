// reports/accident_reaction/service/AccidentReactionServiceImpl.java
package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import com.smooth.driving_analysis_service.reports.accident_reaction.resolver.DrivingResolver;
import com.smooth.driving_analysis_service.reports.accident_reaction.repository.AccidentReactionMetricRepository;
import com.smooth.driving_analysis_service.reports.accident_reaction.repository.AlertRenderEventRepository;
import com.smooth.driving_analysis_service.reports.accident_reaction.entity.AlertRenderEvent;
import com.smooth.driving_analysis_service.reports.accident_reaction.entity.AccidentReactionMetric;
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
    private final AlertRenderEventRepository alertRenderEventRepository;
    private final AccidentReactionWindowAnalyzer analyzer;

    @Override @Transactional
    public Ack recordAndAnalyzeAsync(String alertId, Long userId, long renderedAtMs, String type) {
        String drivingId = resolver.resolveDrivingId(userId, renderedAtMs, 300);
        
        // 밀리초를 LocalDateTime으로 변환
        LocalDateTime renderedAt = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(renderedAtMs), ZoneId.of("Asia/Seoul"));
        
        // AlertRenderEvent 저장
        AlertRenderEvent alertEvent = AlertRenderEvent.builder()
                .alertId(alertId)
                .userId(userId)
                .drivingId(drivingId)
                .type(type)
                .renderedAt(renderedAt)
                .receivedAt(LocalDateTime.now())
                .build();
        alertRenderEventRepository.save(alertEvent);
        
        analyzeAsync(alertId, userId, renderedAtMs, drivingId); // 비동기
        return new Ack(drivingId);
    }

    @Async
    protected void analyzeAsync(String alertId, Long userId, long renderedAtMs, String drivingId) {
        try {
            var res = analyzer.findFirstReactionSessionBound(userId, renderedAtMs, drivingId);
            
            // AccidentReactionMetric 저장
            AccidentReactionMetric metric = AccidentReactionMetric.builder()
                    .alertId(alertId)
                    .userId(userId)
                    .drivingId(drivingId)
                    .responseTimeMs(res.reactionMs() != null ? res.reactionMs().longValue() : null)
                    .responded(res.responded())
                    .decelOrStop(res.decelOrStop())
                    .evasiveManeuver(res.evasiveManeuver())
                    .reactionType(res.eventType())
                    .windowSec(120)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            repo.save(metric);
        } catch (Exception e) {
            log.error("accident_reaction analyzeAsync error alertId={}", alertId, e);
        }
    }

    @Override @Transactional(readOnly = true)
    public Map<String,Object> summary(Long userId, String from, String to) {
        var tz = ZoneId.of("Asia/Seoul");
        var fromTs = LocalDate.parse(from).atStartOfDay(tz);
        var toTs   = LocalDate.parse(to).plusDays(1).atStartOfDay(tz).minusNanos(1);
        
        Object[] stats = repo.summary(userId, fromTs.toLocalDateTime(), toTs.toLocalDateTime());
        
        // 결과 가공
        Map<String, Object> result = new HashMap<>();
        result.put("totalAlerts", stats[0]);
        result.put("avgReactionMs", stats[1]);
        result.put("brakeOrStopRatio", stats[2]);
        result.put("evasiveRatio", stats[3]);
        
        return result;
    }
}
