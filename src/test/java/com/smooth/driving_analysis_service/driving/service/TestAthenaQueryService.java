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
}