package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import com.smooth.driving_analysis_service.reports.accident_reaction.dto.request.AccidentReactionRequestDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.AccidentReactionResponseDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.result.AccidentReactionResultDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.entity.AlertRenderEvent;
import com.smooth.driving_analysis_service.reports.accident_reaction.repository.AccidentReactionMetricRepository;
import com.smooth.driving_analysis_service.reports.accident_reaction.repository.AlertRenderEventRepository;
import com.smooth.driving_analysis_service.reports.accident_reaction.service.DrivingIdResolverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccidentReactionService {

    private final AlertRenderEventRepository renderRepo;
    private final AccidentReactionMetricRepository metricRepo;
    private final ReactionAnalysisService analysisService;
    private final DrivingIdResolverService drivingIdResolver;

    @Transactional
    public AccidentReactionResponseDto recordRendered(String alertId, long userId, AccidentReactionRequestDto req) {
        LocalDateTime renderedAt = Instant.ofEpochMilli(req.getRenderedAtMs())
                .atZone(ZoneId.systemDefault()).toLocalDateTime();

        String drivingId = drivingIdResolver.resolve(userId, renderedAt);

        log.info("[사고반응] 알림 수신: alertId={}, userId={}, type={}, renderedAt={}, drivingId={}",
                alertId, userId, req.getType(), renderedAt, drivingId);

        AlertRenderEvent saved = renderRepo.save(AlertRenderEvent.builder()
                .alertId(alertId)
                .userId(userId)
                .type(req.getType())
                .renderedAt(renderedAt)
                .drivingId(drivingId)
                .build());

        analysisService.scheduleAnalysis(saved);
        log.info("[사고반응] 분석 스케줄링 완료: alertId={}", alertId);

        return AccidentReactionResponseDto.builder()
                .alertId(alertId)
                .userId(userId)
                .drivingId(drivingId)
                .serverReceivedAtMs(Instant.now().toEpochMilli())
                .analysisScheduled(true)
                .build();
    }

    @Transactional(readOnly = true)
    public AccidentReactionResultDto summary(long userId, LocalDateTime from, LocalDateTime to) {
        Object[] row = metricRepo.summary(userId, from, to);
        int alerts = row[0] == null ? 0 : ((Number)row[0]).intValue();
        Long avgMs = row[1] == null ? null : ((Number)row[1]).longValue();
        double brake = row[2] == null ? 0.0 : ((Number)row[2]).doubleValue();
        double evasive = row[3] == null ? 0.0 : ((Number)row[3]).doubleValue();

        return AccidentReactionResultDto.builder()
                .alertsReceived(alerts)
                .avgResponseMs(avgMs)
                .brakeOrStopRatio(brake)
                .evasiveRatio(evasive)
                .build();
    }
}
