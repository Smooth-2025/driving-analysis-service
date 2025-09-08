package com.smooth.driving_analysis_service.reports.dna.service.impl;

import com.smooth.driving_analysis_service.driving.dto.result.DrivingAnalysisResultDto;
import com.smooth.driving_analysis_service.driving.dto.result.EventAnalysisResultDto;
import com.smooth.driving_analysis_service.driving.service.AthenaQueryService;
import com.smooth.driving_analysis_service.reports.dna.service.DnaMetricSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DnaMetricSourceImpl implements DnaMetricSource {

    private final AthenaQueryService athenaQueryService;

    @Override
    public DnaInput loadForReport(Long reportId, List<String> drivingIds) {
        log.info("[DNA] Loading metrics for reportId={}, drivingIds={}", reportId, drivingIds.size());

        List<PerDriving> drivings = drivingIds.stream()
                .map(this::analyzeDriving)
                .collect(Collectors.toList());

        log.info("[DNA] Loaded {} driving metrics", drivings.size());
        return new DnaInput(drivings);
    }

    private PerDriving analyzeDriving(String drivingId) {
        try {
            // S3 원천 데이터에서 주행 분석 결과 조회
            var drivingAnalysis = athenaQueryService.getDrivingAnalysis(drivingId);
            var eventAnalysis = athenaQueryService.getEventAnalysis(drivingId);

            // A축: 0→40km/h 도달시간 계산 (추정)
            Double sec0to40 = calculateAccelerationTime(drivingAnalysis);

            // B축: 평균 감속률 계산 (하드브레이크 기반 추정)
            Double avgDecelRate = calculateDeceleration(eventAnalysis, drivingAnalysis);

            // C축: km당 차선변경 횟수
            Double laneChangePerKm = calculateLaneChangePerKm(eventAnalysis, drivingAnalysis);

            // C축 강화: 차선변경 후 가속률 (추정)
            Double postChangeAccel = calculatePostChangeAcceleration(eventAnalysis);

            // 거리 (km 단위)
            Double distanceKm = drivingAnalysis.getTotalDistance() != null ?
                    drivingAnalysis.getTotalDistance() / 1000.0 : 0.0;

            return new PerDriving(
                    drivingId,
                    sec0to40,
                    avgDecelRate,
                    laneChangePerKm,
                    postChangeAccel,
                    distanceKm
            );

        } catch (Exception e) {
            log.warn("[DNA] Failed to analyze driving {}: {}", drivingId, e.getMessage());
            // 실패 시 기본값 반환
            return new PerDriving(drivingId, null, null, 0.0, null, 0.0);
        }
    }

    /**
     * A축: 0→40km/h 도달시간 추정
     * 실제로는 S3 상세 로그에서 계산해야 하지만, 현재는 평균속도 기반 추정
     */
    private Double calculateAccelerationTime(DrivingAnalysisResultDto drivingAnalysis) {
        if (drivingAnalysis.getAvgSpeed() == null) return null;

        double avgSpeed = drivingAnalysis.getAvgSpeed();

        // 평균속도 기반 가속시간 추정 (경험적 공식)
        // 평균속도가 높을수록 초기 가속이 빠르다고 가정
        if (avgSpeed >= 60) return 4.5; // 빠른 가속
        if (avgSpeed >= 40) return 7.0; // 보통 가속  
        if (avgSpeed >= 20) return 12.0; // 느린 가속
        return 15.0; // 매우 느린 가속
    }

    /**
     * B축: 평균 감속률 추정
     * 실제로는 S3에서 감속 이벤트별 Δv/Δt를 계산해야 함
     */
    private Double calculateDeceleration(EventAnalysisResultDto eventAnalysis, DrivingAnalysisResultDto drivingAnalysis) {
        if (drivingAnalysis.getTotalDistance() == null || drivingAnalysis.getTotalDistance() <= 0) {
            return null;
        }

        double distanceKm = drivingAnalysis.getTotalDistance() / 1000.0;
        double hardBrakePerKm = eventAnalysis.getHardBrakeCount() / distanceKm;

        // 하드브레이크 빈도를 감속률로 변환 (경험적 공식)
        if (hardBrakePerKm >= 0.5) return 2.2; // 급감속
        if (hardBrakePerKm >= 0.2) return 1.5; // 보통 감속
        if (hardBrakePerKm >= 0.05) return 1.0; // 부드러운 감속
        return 0.6; // 매우 부드러운 감속
    }

    /**
     * C축: km당 차선변경 횟수
     */
    private Double calculateLaneChangePerKm(EventAnalysisResultDto eventAnalysis, DrivingAnalysisResultDto drivingAnalysis) {
        if (drivingAnalysis.getTotalDistance() == null || drivingAnalysis.getTotalDistance() <= 0) {
            return 0.0;
        }

        double distanceKm = drivingAnalysis.getTotalDistance() / 1000.0;
        return (double) eventAnalysis.getLaneChangeCount() / distanceKm;
    }

    /**
     * C축 강화: 차선변경 후 가속률 추정
     * 실제로는 S3에서 차선변경 후 0~5초간 가속도 변화를 분석해야 함
     */
    private Double calculatePostChangeAcceleration(EventAnalysisResultDto eventAnalysis) {
        if (eventAnalysis.getLaneChangeCount() == 0) return null;

        // 급가속 이벤트와 차선변경의 비율로 추정
        double ratio = (double) eventAnalysis.getRapidAccelCount() / eventAnalysis.getLaneChangeCount();

        if (ratio >= 0.8) return 0.45; // 공격적 가속
        if (ratio >= 0.4) return 0.25; // 보통 가속
        if (ratio >= 0.1) return 0.12; // 부드러운 가속
        return 0.05; // 매우 부드러운 가속
    }
}