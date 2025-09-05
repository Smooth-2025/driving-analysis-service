package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import com.smooth.driving_analysis_service.reports.accident_reaction.entity.AccidentReactionMetric;
import com.smooth.driving_analysis_service.reports.accident_reaction.entity.AlertRenderEvent;
import com.smooth.driving_analysis_service.reports.accident_reaction.repository.AccidentReactionMetricRepository;
import com.smooth.driving_analysis_service.reports.accident_reaction.service.AthenaReactionAnalyzerService;
import com.smooth.driving_analysis_service.reports.accident_reaction.support.DrivingEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReactionAnalysisService {

    private final AccidentReactionMetricRepository metricRepo;
    private final AthenaReactionAnalyzerService athena;

    @Value("${accident.reaction.analysis.windowSec:120}")
    private int analysisWindowSec;

    @Value("${accident.reaction.athena.enabled:true}")
    private boolean athenaEnabled;

    @Async
    @Transactional
    public void scheduleAnalysis(AlertRenderEvent render) {
        try {
            Instant from = render.getRenderedAt().atZone(ZoneId.systemDefault()).toInstant();
            Instant to   = from.plusSeconds(analysisWindowSec);

            log.info("[사고반응] 분석 시작: alertId={}, userId={}, drivingId={}, windowSec={}",
                    render.getAlertId(), render.getUserId(), render.getDrivingId(), analysisWindowSec);

            Optional<AthenaReactionAnalyzerService.FoundEvent> found =
                    (render.getDrivingId() != null && !render.getDrivingId().isBlank())
                            ? athena.findEarliestEvent(render.getUserId(), render.getDrivingId(), from, to)
                            : athena.findEarliestEventLoose(render.getUserId(), from, to);

            Integer reactionMs = null;
            boolean responded = false;
            boolean decelOrStop = false;
            boolean evasive = false;

            if (found.isPresent()) {
                var ev = found.get();
                responded  = true;
                reactionMs = Math.toIntExact(ev.eventInstant().toEpochMilli() - from.toEpochMilli());

                DrivingEventType t = ev.type();
                if (t != null) {
                    switch (t) {
                        case HARD_BRAKE -> decelOrStop = true;
                        case LANE_CHANGE, SHARP_TURN -> evasive = true;
                        case RAPID_ACCEL -> { /* 정책상 플래그 미설정(필요시 변경) */ }
                    }
                }

                log.info("[사고반응] 최초 이벤트 발견: alertId={}, eventType={}, eventDrivingId={}, reactionMs={}",
                        render.getAlertId(), t, ev.drivingId(), reactionMs);
            } else {
                log.warn("[사고반응] 윈도우 내 이벤트 미발견: alertId={}, windowSec={}",
                        render.getAlertId(), analysisWindowSec);
            }

            metricRepo.save(AccidentReactionMetric.builder()
                    .alertId(render.getAlertId())
                    .userId(render.getUserId())
                    .responseTimeMs(reactionMs != null ? reactionMs.longValue() : null)
                    .responded(responded)
                    .decelOrStop(decelOrStop)
                    .evasiveManeuver(evasive)
                    .windowSec(analysisWindowSec)
                    .build());

            log.info("[사고반응] 메트릭 저장 완료: alertId={}, 반응여부={}, 반응시간(ms)={}",
                    render.getAlertId(), responded, reactionMs);

        } catch (Exception e) {
            log.error("[사고반응] 분석 실패: alertId={}, 사유={}", render.getAlertId(), e.getMessage(), e);
        }
    }
}
