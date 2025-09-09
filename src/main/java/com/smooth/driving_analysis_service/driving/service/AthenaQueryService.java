package com.smooth.driving_analysis_service.driving.service;

import com.smooth.driving_analysis_service.driving.dto.result.DrivingAnalysisResultDto;
import com.smooth.driving_analysis_service.driving.dto.result.EventAnalysisResultDto;


public interface AthenaQueryService {

    DrivingAnalysisResultDto getDrivingAnalysis(String drivingId);

    EventAnalysisResultDto getEventAnalysis(String drivingId);
}
