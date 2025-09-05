package com.smooth.driving_analysis_service.driving.service;

import com.smooth.driving_analysis_service.driving.dto.result.DrivingAnalysisResultDto;
import com.smooth.driving_analysis_service.driving.dto.result.EventAnalysisResultDto;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class TestAthenaQueryService implements AthenaQueryService {

    @Override
    public DrivingAnalysisResultDto getDrivingAnalysis(String drivingId) {
        return DrivingAnalysisResultDto.builder()
                .totalDistance(100.0)
                .avgSpeed(50.0)
                .maxSpeed(80.0)
                .minSpeed(20.0)
                .cruiseRatio(0.7)
                .build();
    }

    @Override
    public EventAnalysisResultDto getEventAnalysis(String drivingId) {
        return EventAnalysisResultDto.builder()
                .laneChangeCount(5)
                .hardBrakeCount(2)
                .rapidAccelCount(3)
                .sharpTurnCount(1)
                .build();
    }

    @Override
    public java.util.List<java.util.Map<String, Object>> executeQuery(String query) {
        // 테스트용 더미 데이터 반환
        if (query.contains("hard_brake") || query.contains("lane_change") || query.contains("sharp_turn")) {
            // 사고 반응 분석용 더미 이벤트 데이터
            return java.util.List.of(
                java.util.Map.of(
                    "event_type", "hard_brake",
                    "driving_id", "test-driving-001",
                    "event_ts", "2025-01-09T10:01:30Z",
                    "user_id", 12345L
                )
            );
        }
        
        // 기본적으로 빈 리스트 반환
        return java.util.List.of();
    }
}