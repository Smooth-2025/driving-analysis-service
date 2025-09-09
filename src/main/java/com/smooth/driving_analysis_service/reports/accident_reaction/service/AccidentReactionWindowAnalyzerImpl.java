package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import com.smooth.driving_analysis_service.reports.common.service.ReportsAthenaQueryService;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.Reaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccidentReactionWindowAnalyzerImpl implements AccidentReactionWindowAnalyzer {

    private final ReportsAthenaQueryService reportsAthenaQueryService;

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

            // 3. 속도 변화 패턴 분석으로 반응 감지
            Optional<ReactionAnalysisResult> reactionResult = analyzeSpeedChangePattern(events, renderedAtMs);

            if (reactionResult.isEmpty()) {
                log.debug("No significant reaction pattern found: userId={}, drivingId={}", userId, drivingId);
                return new Reaction(false, null, null, false, false);
            }

            // 4. 반응시간 계산 및 타입 분류
            ReactionAnalysisResult result = reactionResult.get();
            int reactionMs = result.reactionTimeMs;
            String reactionType = result.reactionType;

            // 반응시간이 음수이거나 너무 크면 무효 처리
            if (reactionMs < 0 || reactionMs > 120000) {
                log.warn("Invalid reaction time: {}ms, treating as no reaction", reactionMs);
                return new Reaction(false, null, null, false, false);
            }

            boolean decelOrStop = result.isDeceleration;
            boolean evasiveManeuver = result.isEvasiveManeuver;

            log.debug("Reaction found: reactionType={}, reactionMs={}, decelOrStop={}, evasiveManeuver={}",
                    reactionType, reactionMs, decelOrStop, evasiveManeuver);

            return new Reaction(true, (long) reactionMs, reactionType, decelOrStop, evasiveManeuver);

        } catch (Exception e) {
            log.error("Failed to analyze reaction window: userId={}, drivingId={}", userId, drivingId, e);
            return new Reaction(false, null, null, false, false);
        }
    }

    /**
     * S3에서 윈도우 내 이벤트 조회 - 실제 driving_data 형식에 맞게 수정
     */
    private List<Map<String, Object>> queryEventsInWindow(Long userId, String drivingId,
            Instant windowStart, Instant windowEnd) {
        String query = """
                SELECT
                    eventType as event_type,
                    tripId as driving_id,
                    timestamp as event_ts,
                    vehicleId,
                    speed,
                    locationX,
                    locationY
                FROM driving_data
                WHERE timestamp BETWEEN '%s' AND '%s'
                  AND eventType IN ('driving_update', 'driving_end')
                  AND (%s IS NULL OR tripId = '%s')
                ORDER BY timestamp ASC
                LIMIT 50
                """.formatted(
                windowStart.toString(),
                windowEnd.toString(),
                drivingId == null ? "TRUE" : "FALSE",
                drivingId != null ? drivingId : "");

        try {
            return reportsAthenaQueryService.executeQuery(query);
        } catch (Exception e) {
            log.error("Failed to query events from S3: userId={}, drivingId={}", userId, drivingId, e);
            return List.of();
        }
    }

    /**
     * 속도 변화 패턴을 분석하여 반응을 감지
     */
    private Optional<ReactionAnalysisResult> analyzeSpeedChangePattern(List<Map<String, Object>> events,
            long alertTimeMs) {
        if (events.size() < 2) {
            return Optional.empty();
        }

        // 이벤트들을 시간순으로 정렬
        events.sort(Comparator.comparing(e -> parseTimestamp((String) e.get("event_ts"))));

        for (int i = 1; i < events.size(); i++) {
            Map<String, Object> prevEvent = events.get(i - 1);
            Map<String, Object> currEvent = events.get(i);

            double prevSpeed = parseSpeed(prevEvent.get("speed"));
            double currSpeed = parseSpeed(currEvent.get("speed"));
            Instant currTime = parseTimestamp((String) currEvent.get("event_ts"));

            // 급격한 속도 변화 감지 (15km/h 이상 감소 또는 증가)
            double speedDiff = currSpeed - prevSpeed;

            if (Math.abs(speedDiff) >= 15) {
                int reactionTimeMs = (int) (currTime.toEpochMilli() - alertTimeMs);

                if (reactionTimeMs >= 0 && reactionTimeMs <= 120000) {
                    String reactionType = speedDiff < 0 ? "hard_brake" : "acceleration";
                    boolean isDeceleration = speedDiff < 0;
                    boolean isEvasive = detectEvasiveManeuver(prevEvent, currEvent);

                    return Optional.of(new ReactionAnalysisResult(
                            reactionTimeMs, reactionType, isDeceleration, isEvasive));
                }
            }

            // 완전 정지 감지
            if (prevSpeed > 5 && currSpeed == 0) {
                int reactionTimeMs = (int) (currTime.toEpochMilli() - alertTimeMs);

                if (reactionTimeMs >= 0 && reactionTimeMs <= 120000) {
                    return Optional.of(new ReactionAnalysisResult(
                            reactionTimeMs, "emergency_stop", true, false));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * 회피 기동 감지 (위치 변화 패턴 분석)
     */
    private boolean detectEvasiveManeuver(Map<String, Object> prevEvent, Map<String, Object> currEvent) {
        try {
            double prevX = parseDouble(prevEvent.get("locationX"));
            double prevY = parseDouble(prevEvent.get("locationY"));
            double currX = parseDouble(currEvent.get("locationX"));
            double currY = parseDouble(currEvent.get("locationY"));

            // 급격한 방향 변화 감지 (10m 이상의 측면 이동)
            double lateralMovement = Math.abs(currX - prevX) + Math.abs(currY - prevY);
            return lateralMovement > 10.0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 속도 값 파싱
     */
    private double parseSpeed(Object speedObj) {
        if (speedObj == null)
            return 0.0;
        try {
            return Double.parseDouble(speedObj.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Double 값 파싱
     */
    private double parseDouble(Object obj) {
        if (obj == null)
            return 0.0;
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * 반응 분석 결과를 담는 내부 클래스
     */
    private static class ReactionAnalysisResult {
        final int reactionTimeMs;
        final String reactionType;
        final boolean isDeceleration;
        final boolean isEvasiveManeuver;

        ReactionAnalysisResult(int reactionTimeMs, String reactionType,
                boolean isDeceleration, boolean isEvasiveManeuver) {
            this.reactionTimeMs = reactionTimeMs;
            this.reactionType = reactionType;
            this.isDeceleration = isDeceleration;
            this.isEvasiveManeuver = isEvasiveManeuver;
        }
    }

    /**
     * 타임스탬프 파싱 (실제 driving_data 형식에 맞게 개선)
     */
    private Instant parseTimestamp(String timestamp) {
        if (timestamp == null) {
            return Instant.now();
        }

        try {
            // ISO 8601 형식 (2025-09-09T10:37:13+09:00)
            return Instant.parse(timestamp);
        } catch (Exception e) {
            try {
                // 밀리초 형식 시도
                return Instant.ofEpochMilli(Long.parseLong(timestamp));
            } catch (Exception ex) {
                try {
                    // 다른 ISO 형식들 시도
                    return Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(timestamp));
                } catch (Exception ex2) {
                    log.warn("Failed to parse timestamp: {}, using current time", timestamp);
                    return Instant.now();
                }
            }
        }
    }
}