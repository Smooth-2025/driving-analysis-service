package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.Reaction;
import com.smooth.driving_analysis_service.reports.accident_reaction.entity.AlertRenderEvent;
import com.smooth.driving_analysis_service.reports.accident_reaction.entity.AccidentReactionMetric;
import com.smooth.driving_analysis_service.reports.accident_reaction.repository.AlertRenderEventRepository;
import com.smooth.driving_analysis_service.reports.accident_reaction.repository.AccidentReactionMetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccidentReactionBatchServiceImpl implements AccidentReactionBatchService {
    
    private final AlertRenderEventRepository alertRenderEventRepository;
    private final AccidentReactionMetricRepository accidentReactionMetricRepository;
    private final AccidentReactionWindowAnalyzer windowAnalyzer;
    
    @Override
    @Transactional
    public void generateInterimReport(Long reportId, Long userId, List<String> drivingIds) {
        log.info("Generating accident reaction interim report: reportId={}, userId={}, drivingCount={}", 
                reportId, userId, drivingIds.size());
        
        try {
            processAlertReactions(reportId, userId, drivingIds, false);
            log.info("Accident reaction interim report generated successfully: reportId={}", reportId);
        } catch (Exception e) {
            log.error("Failed to generate accident reaction interim report: reportId={}", reportId, e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void generateFinalReport(Long reportId, Long userId, List<String> drivingIds) {
        log.info("Generating accident reaction final report: reportId={}, userId={}, drivingCount={}", 
                reportId, userId, drivingIds.size());
        
        try {
            processAlertReactions(reportId, userId, drivingIds, true);
            log.info("Accident reaction final report generated successfully: reportId={}", reportId);
        } catch (Exception e) {
            log.error("Failed to generate accident reaction final report: reportId={}", reportId, e);
            throw e;
        }
    }
    
    /**
     * 알림 반응 분석 처리
     */
    private void processAlertReactions(Long reportId, Long userId, List<String> drivingIds, boolean isFinal) {
        // 1. 해당 주행들의 AlertRenderEvent 조회
        List<AlertRenderEvent> alertEvents = alertRenderEventRepository.findByDrivingIdIn(drivingIds);
        
        if (alertEvents.isEmpty()) {
            log.info("No alert events found for drivingIds: {}", drivingIds);
            return;
        }
        
        log.info("Found {} alert events for analysis", alertEvents.size());
        
        // 2. 각 알림별로 S3 이벤트 분석
        for (AlertRenderEvent alertEvent : alertEvents) {
            try {
                processAlertReaction(alertEvent, isFinal);
            } catch (Exception e) {
                log.error("Failed to process alert reaction: alertId={}", alertEvent.getAlertId(), e);
                // 개별 알림 처리 실패는 전체를 중단하지 않음
            }
        }
    }
    
    /**
     * 개별 알림 반응 분석
     */
    private void processAlertReaction(AlertRenderEvent alertEvent, boolean isFinal) {
        String alertId = alertEvent.getAlertId();
        
        // 이미 분석된 결과가 있는지 확인
        if (accidentReactionMetricRepository.existsByAlertId(alertId)) {
            if (isFinal) {
                log.debug("Alert reaction already analyzed, skipping: alertId={}", alertId);
                return;
            }
            // INTERIM의 경우 기존 결과 삭제 후 재분석
            accidentReactionMetricRepository.deleteByAlertId(alertId);
        }
        
        // S3 이벤트 데이터 분석
        long renderedAtMs = alertEvent.getRenderedAt()
                .atZone(java.time.ZoneId.of("Asia/Seoul"))
                .toInstant()
                .toEpochMilli();
        
        Reaction reaction = windowAnalyzer.findFirstReactionSessionBound(
                alertEvent.getUserId(), 
                renderedAtMs, 
                alertEvent.getDrivingId()
        );
        
        // AccidentReactionMetric 저장
        AccidentReactionMetric metric = AccidentReactionMetric.builder()
                .alertId(alertId)
                .userId(alertEvent.getUserId())
                .drivingId(alertEvent.getDrivingId())
                .reactionMs(reaction.getReactionMs() != null ? reaction.getReactionMs().intValue() : null)
                .reacted(reaction.isResponded())
                .decelOrStop(reaction.isDecelOrStop())
                .evasiveManeuver(reaction.isEvasiveManeuver())
                .eventType(reaction.getEventType())
                .windowSec(120)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        accidentReactionMetricRepository.save(metric);
        
        log.debug("Alert reaction analyzed: alertId={}, responded={}, reactionMs={}", 
                alertId, reaction.isResponded(), reaction.getReactionMs());
    }
}