package com.smooth.driving_analysis_service.reports.dna.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public interface DnaMetricSource {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerDriving {
        private String drivingId;
        private Double sec0to40;         // A: 0→40km/h 도달시간(초)
        private Double avgDecelRate;     // B: 평균 감속율(Δv/Δt, m/s^2)
        private Double laneChangePerKm;  // C: km당 차선변경 수
        private Double postChangeAccel;  // C 강화: 변경 후 0~5초 평균 가속(m/s^2)
        private Double distanceKm;       // 가중/정규화용 거리
    }

    @Data
    @AllArgsConstructor
    public static class ReportMetrics {
        private List<PerDriving> drivings;
    }

    /** reportId에 해당하는 drivingIds를 대상으로, S3 원천 로그에서 계산한 per-driving 메트릭을 제공 */
    ReportMetrics loadForReport(Long reportId, List<String> drivingIds);
}
