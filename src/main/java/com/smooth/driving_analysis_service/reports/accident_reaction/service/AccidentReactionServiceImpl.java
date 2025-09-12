package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import com.smooth.driving_analysis_service.reports.accident_reaction.dto.request.AlertRenderRequestDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.AccidentReactionReportResponseDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.entity.AlertRenderEvent;
import com.smooth.driving_analysis_service.reports.accident_reaction.entity.AccidentReactionMetric;
import com.smooth.driving_analysis_service.reports.accident_reaction.repository.AlertRenderEventRepository;
import com.smooth.driving_analysis_service.reports.accident_reaction.repository.AccidentReactionMetricRepository;
import com.smooth.driving_analysis_service.reports.common.service.ReportsAthenaQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccidentReactionServiceImpl implements AccidentReactionService {
    
    private final AlertRenderEventRepository alertRenderEventRepository;
    private final AccidentReactionMetricRepository accidentReactionMetricRepository;
    private final ReportsAthenaQueryService athenaQueryService;
    
    @Override
    @Transactional
    public void processAlertRender(String alertId, AlertRenderRequestDto request) {
        log.info("Processing alert render: alertId={}, renderedAtMs={}", alertId, request.getRenderedAtMs());
        
        try {
            // 1. S3에서 해당 시간이 포함된 drivingId 찾기
            String drivingId = findDrivingIdByTimestamp(request.getRenderedAtMs());
            
            if (drivingId == null) {
                log.warn("No drivingId found for timestamp: {}", request.getRenderedAtMs());
                return;
            }
            
            // 2. AlertRenderEvent 저장
            AlertRenderEvent alertEvent = AlertRenderEvent.builder()
                    .alertId(alertId)
                    .drivingId(drivingId)
                    .renderedAtMs(request.getRenderedAtMs())
                    .alertType(request.getType())
                    .build();
            
            alertRenderEventRepository.save(alertEvent);
            
            // 3. S3에서 반응 데이터 분석 및 AccidentReactionMetric 저장
            analyzeAndSaveReactionMetric(alertId, drivingId, request.getRenderedAtMs());
            
        } catch (Exception e) {
            log.error("Failed to process alert render: alertId={}", alertId, e);
            throw e;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public AccidentReactionReportResponseDto getAccidentReactionReport(String reportId) {
        log.info("Getting accident reaction report for reportId: {}", reportId);
        
        try {
            Long reportIdLong = Long.parseLong(reportId);
            
            // 기본 지표 조회
            Object[] basicMetrics = accidentReactionMetricRepository.getBasicMetricsByReportId(reportIdLong);
            
            if (basicMetrics == null) {
                log.warn("No accident reaction data found for reportId: {}", reportId);
                return createEmptyResponse(reportId);
            }
            
            // 기본 지표 파싱
            Long receivedAlertCount = (Long) basicMetrics[0];
            Double avgReactionSec = (Double) basicMetrics[1];
            Double brakeOrStopRatio = (Double) basicMetrics[2];
            Double avoidRatio = (Double) basicMetrics[3];
            
            // 전체 사용자 평균 계산
            Double globalAvgSec = accidentReactionMetricRepository.getGlobalAverageReactionTime();
            if (globalAvgSec == null) {
                globalAvgSec = 56.0; // 기본값 (이미지 기준)
            }
            
            double myAvgSec = avgReactionSec != null ? avgReactionSec : 0.0;
            int deltaSec = (int) Math.round(globalAvgSec - myAvgSec); // 양수면 더 빠름, 음수면 더 느림
            
            // 차트 데이터 생성
            AccidentReactionReportResponseDto.Chart chart = AccidentReactionReportResponseDto.Chart.builder()
                    .labels(Arrays.asList("일반 운전자", "내 주행"))
                    .valuesSec(Arrays.asList((int) Math.round(globalAvgSec), (int) Math.round(myAvgSec)))
                    .build();
            
            // 벤치마크 데이터 생성
            AccidentReactionReportResponseDto.Benchmark benchmark = AccidentReactionReportResponseDto.Benchmark.builder()
                    .deltaSec(deltaSec)
                    .chart(chart)
                    .build();
            
            // 최종 응답 생성
            return AccidentReactionReportResponseDto.builder()
                    .reportId(reportId)
                    .receivedAlertCount(receivedAlertCount != null ? receivedAlertCount.intValue() : 0)
                    .avgReactionSec(Math.round(myAvgSec * 10.0) / 10.0) // 소수점 1자리 반올림
                    .brakeOrStopRatio(brakeOrStopRatio != null ? brakeOrStopRatio : 0.0)
                    .avoidRatio(avoidRatio != null ? avoidRatio : 0.0)
                    .benchmark(benchmark)
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed to get accident reaction report for reportId: {}", reportId, e);
            return createEmptyResponse(reportId);
        }
    }
    
    /**
     * S3에서 타임스탬프에 해당하는 drivingId 찾기
     */
    private String findDrivingIdByTimestamp(Long timestampMs) {
        try {
            // S3에서 driving_message 데이터를 조회하여 해당 시간대의 drivingId(tripId) 찾기
            String query = String.format("""
                SELECT DISTINCT tripId 
                FROM driving_messages 
                WHERE timestamp BETWEEN %d AND %d
                LIMIT 1
                """, timestampMs - 5000, timestampMs + 5000); // ±5초 범위
            
            List<Map<String, Object>> results = athenaQueryService.executeQuery(query);
            
            if (!results.isEmpty()) {
                return (String) results.get(0).get("tripId");
            }
            
            return null;
        } catch (Exception e) {
            log.error("Failed to find drivingId by timestamp: {}", timestampMs, e);
            return null;
        }
    }
    
    /**
     * S3 데이터를 분석하여 반응 지표 계산 및 저장
     */
    private void analyzeAndSaveReactionMetric(String alertId, String drivingId, Long renderedAtMs) {
        try {
            // S3에서 알림 이후 반응 데이터 조회
            String query = String.format("""
                SELECT eventType, timestamp 
                FROM report_messages 
                WHERE tripId = '%s' 
                AND timestamp > %d 
                AND timestamp < %d
                AND eventType IN ('hard_brake', 'lane_change', 'sharp_turn')
                ORDER BY timestamp ASC
                LIMIT 1
                """, drivingId, renderedAtMs, renderedAtMs + 10000); // 10초 이내 반응만 체크
            
            List<Map<String, Object>> results = athenaQueryService.executeQuery(query);
            
            boolean reacted = !results.isEmpty();
            Long reactionMs = null;
            String reactionType = null;
            boolean decelOrStop = false;
            boolean evasiveManeuver = false;
            
            if (reacted) {
                Map<String, Object> reaction = results.get(0);
                Long reactionTimestamp = (Long) reaction.get("timestamp");
                reactionMs = reactionTimestamp - renderedAtMs;
                reactionType = (String) reaction.get("eventType");
                
                // 반응 타입별 분류
                if ("hard_brake".equals(reactionType)) {
                    decelOrStop = true;
                } else if ("lane_change".equals(reactionType) || "sharp_turn".equals(reactionType)) {
                    evasiveManeuver = true;
                }
            }
            
            // AccidentReactionMetric 저장
            AccidentReactionMetric metric = AccidentReactionMetric.builder()
                    .alertId(alertId)
                    .drivingId(drivingId)
                    .reactionMs(reactionMs)
                    .reacted(reacted)
                    .reactionType(reactionType)
                    .decelOrStop(decelOrStop)
                    .evasiveManeuver(evasiveManeuver)
                    .build();
            
            accidentReactionMetricRepository.save(metric);
            
        } catch (Exception e) {
            log.error("Failed to analyze reaction metric: alertId={}, drivingId={}", alertId, drivingId, e);
        }
    }
    
    private AccidentReactionReportResponseDto createEmptyResponse(String reportId) {
        // 전체 사용자 평균 조회
        Double globalAvgSec = accidentReactionMetricRepository.getGlobalAverageReactionTime();
        if (globalAvgSec == null) {
            globalAvgSec = 56.0; // 기본값
        }
        
        AccidentReactionReportResponseDto.Chart chart = AccidentReactionReportResponseDto.Chart.builder()
                .labels(Arrays.asList("일반 운전자", "내 주행"))
                .valuesSec(Arrays.asList((int) Math.round(globalAvgSec), 0))
                .build();
                
        AccidentReactionReportResponseDto.Benchmark benchmark = AccidentReactionReportResponseDto.Benchmark.builder()
                .deltaSec((int) Math.round(globalAvgSec))
                .chart(chart)
                .build();
                
        return AccidentReactionReportResponseDto.builder()
                .reportId(reportId)
                .receivedAlertCount(0)
                .avgReactionSec(0.0)
                .brakeOrStopRatio(0.0)
                .avoidRatio(0.0)
                .benchmark(benchmark)
                .build();
    }
}