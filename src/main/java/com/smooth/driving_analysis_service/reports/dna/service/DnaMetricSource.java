package com.smooth.driving_analysis_service.reports.dna.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DnaMetricSource {

    /**
     * 리포트용 메트릭 데이터 로딩
     */
    public ReportMetrics loadForReport(Long reportId, List<String> drivingIds) {
        // TODO: S3나 다른 원천 데이터에서 상세 메트릭 로딩
        // 현재는 더미 데이터 반환
        
        List<PerDriving> drivings = drivingIds.stream()
                .map(this::createDummyPerDriving)
                .toList();
        
        return new ReportMetrics(drivings);
    }

    private PerDriving createDummyPerDriving(String drivingId) {
        return PerDriving.builder()
                .drivingId(drivingId)
                .distanceKm(15.0) // 15km
                .laneChangePerKm(1.2) // 1.2회/km
                .postChangeAccel(0.8) // 차선변경 후 가속도
                .sec0to40(6.5) // 0-40km/h 가속 시간
                .avgDecelRate(1.2) // 평균 감속률
                .build();
    }

    public static class ReportMetrics {
        private final List<PerDriving> drivings;

        public ReportMetrics(List<PerDriving> drivings) {
            this.drivings = drivings;
        }

        public List<PerDriving> drivings() {
            return drivings;
        }
    }

    @lombok.Builder
    @lombok.Getter
    public static class PerDriving {
        private String drivingId;
        private Double distanceKm;
        private Double laneChangePerKm;
        private Double postChangeAccel;
        private Double sec0to40;
        private Double avgDecelRate;
    }
}