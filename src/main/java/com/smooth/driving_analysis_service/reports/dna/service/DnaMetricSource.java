package com.smooth.driving_analysis_service.reports.dna.service;

import java.util.List;

public interface DnaMetricSource {

    // per-driving 원천 기반 메트릭
    record PerDriving(
            String drivingId,
            Double sec0to40,         // A: 0→40km/h 도달시간(초)
            Double avgDecelRate,     // B: 평균 감속율(Δv/Δt, m/s^2)
            Double laneChangePerKm,  // C: km당 차선변경 수
            Double postChangeAccel,  // C 강화: 변경 후 0~5초 평균 가속(m/s^2)
            Double distanceKm        // 가중/정규화용 거리
    ) {}

    record DnaInput(List<PerDriving> drivings) {}

    /** reportId에 해당하는 drivingIds를 대상으로, S3 원천 로그에서 계산한 per-driving 메트릭을 제공 */
    DnaInput loadForReport(Long reportId, List<String> drivingIds);
    
    /** 기존 방식 - reportId 기반 */
    java.util.Map<String, Double> getMetrics(Long reportId);
    
    /** 새로운 방식 - drivingIds 기반 */
    java.util.Map<String, Double> getMetricsByDrivingIds(List<String> drivingIds);
}
