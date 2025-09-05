package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import com.smooth.driving_analysis_service.driving.service.AthenaQueryService;
import com.smooth.driving_analysis_service.reports.accident_reaction.support.DrivingEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccidentReactionWindowAnalyzerImpl implements AccidentReactionWindowAnalyzer {
    
    private final AthenaQueryService athenaQueryService;
    
    @Override
    public Reaction findFirstReactionSessionBound(Long userId, long renderedAtMs, String drivingId) {
        try {
            // 1. 윈도우 설정: renderedAtMs ~ renderedAtMs + 120초
            Instant windowStart = Instant.ofEpochMilli(renderedAtMs);
            Instant windowEnd = windowStart.plusSeconds(120);
            
            log.debug("Analyzing reaction window: userId={}, drivingId={}, window={}~{}", 
                    userId, drivingId, windowStart, windowEnd);
            
            // 2. Athena 쿼리로 S3에서 이벤트 조회
            List<Map<String, Object>> events = queryEventsInWindow(userId, drivingId, windowStart, windowEnd);
            
            if (events.isEmpty()) {
                log.debug("No reaction events found in window: userId={}, drivingId={}", userId, drivingId);
                return new Reaction(false, null, null, false, false);
            }
            
            // 3. 가장 빠른 반응 이벤트 찾기
            Optional<Map<String, Object>> firstReaction = events.stream()
                    .filter(this::isReactionEvent)
                    .min(Comparator.comparing(e -> Instant.parse((String) e.get("event_ts"))))
                    .or(() -> events.stream().min(Comparator.comparing(e -> Instant.parse((String) e.get("event_ts")))));
            
            if (firstReaction.isEmpty()) {
                return new Reaction(false, null, null, false, false);
            }
            
            // 4. 반응시간 계산 및 타입 분류
            Map<String, Object> event = firstReaction.get();
            Instant eventTs = Instant.parse((String) event.get("event_ts"));
            int reactionMs = (int) (eventTs.toEpochMilli() - renderedAtMs);
            String eventType = (String) event.get("event_type");
            
            // 반응시간이 음수이거나 너무 크면 무효 처리
            if (reactionMs < 0 || reactionMs > 120000) {
                log.warn("Invalid reaction time: {}ms, treating as no reaction", reactionMs);
                return new Reaction(false, null, null, false, false);
            }
            
            boolean decelOrStop = isDecelOrStop(eventType);
            boolean evasiveManeuver = isEvasiveManeuver(eventType);
            
            log.debug("Reaction found: eventType={}, reactionMs={}, decelOrStop={}, evasiveManeuver={}", 
                    eventType, reactionMs, decelOrStop, evasiveManeuver);
            
            return new Reaction(true, reactionMs, eventType, decelOrStop, evasiveManeuver);
            
        } catch (Exception e) {
            log.error("Failed to analyze reaction window: userId={}, drivingId={}", userId, drivingId, e);
            return new Reaction(false, null, null, false, false);
        }
    }
    
    /**
     * S3에서 윈도우 내 이벤트 조회
     */
    private List<Map<String, Object>> queryEventsInWindow(Long userId, String drivingId, 
                                                         Instant windowStart, Instant windowEnd) {
        String query = """
            SELECT 
                event_type,
                driving_id,
                event_ts,
                user_id
            FROM event_data 
            WHERE user_id = %d
              AND event_ts BETWEEN '%s' AND '%s'
              AND event_type IN ('hard_brake', 'lane_change', 'sharp_turn')
              AND (%s IS NULL OR driving_id = '%s')
            ORDER BY event_ts ASC
            LIMIT 10
            """.formatted(
                userId,
                windowStart.toString(),
                windowEnd.toString(),
                drivingId == null ? "TRUE" : "FALSE",
                drivingId != null ? drivingId : ""
            );
        
        try {
            return athenaQueryService.executeQuery(query);
        } catch (Exception e) {
            log.error("Failed to query events from S3: userId={}, drivingId={}", userId, drivingId, e);
            return List.of();
        }
    }
    
    /**
     * 반응 이벤트인지 확인
     */
    private boolean isReactionEvent(Map<String, Object> event) {
        String eventType = (String) event.get("event_type");
        return eventType != null && (
                eventType.equals("hard_brake") ||
                eventType.equals("lane_change") ||
                eventType.equals("sharp_turn")
        );
    }
    
    /**
     * 감속/정지 반응인지 확인
     */
    private boolean isDecelOrStop(String eventType) {
        return "hard_brake".equals(eventType);
    }
    
    /**
     * 회피 반응인지 확인
     */
    private boolean isEvasiveManeuver(String eventType) {
        return "lane_change".equals(eventType) || "sharp_turn".equals(eventType);
    }
}