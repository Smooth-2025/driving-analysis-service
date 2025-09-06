package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccidentReactionWindowAnalyzer {

    public ReactionResult findFirstReactionSessionBound(Long userId, long renderedAtMs, String drivingId) {
        // TODO: 실제 S3 데이터 분석 로직 구현
        // 현재는 더미 데이터 반환
        
        log.debug("Analyzing reaction for userId: {}, drivingId: {}, renderedAt: {}", 
                userId, drivingId, renderedAtMs);
        
        // 더미 분석 결과
        return new ReactionResult(
                true,           // responded
                1500,           // reactionMs
                "hard_brake",   // eventType
                true,           // decelOrStop
                false           // evasiveManeuver
        );
    }

    public record ReactionResult(
            boolean responded,
            Integer reactionMs,
            String eventType,
            boolean decelOrStop,
            boolean evasiveManeuver
    ) {}
}