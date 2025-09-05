package com.smooth.driving_analysis_service.driving.service;

import com.smooth.driving_analysis_service.driving.dto.request.DrivingCharacterAnalysisRequestDto;
import com.smooth.driving_analysis_service.driving.dto.result.DrivingCharacterAnalysisResultDto;

public interface BedrockService {

    DrivingCharacterAnalysisResultDto invokeModel(DrivingCharacterAnalysisRequestDto requestDto);
}
