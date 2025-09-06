package com.smooth.driving_analysis_service.driving.controller;

import com.smooth.driving_analysis_service.driving.dto.request.DrivingCharacterAnalysisRequestDto;
import com.smooth.driving_analysis_service.driving.dto.result.DrivingCharacterAnalysisResultDto;
import com.smooth.driving_analysis_service.driving.service.BedrockService;
import com.smooth.driving_analysis_service.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Profile("dev")
@RequiredArgsConstructor
@RequestMapping("/api/driving-analysis")
@RestController
public class BedrockController {

    private final BedrockService bedrockService;

    @PostMapping("/bedrock-test")
    public ResponseEntity<ApiResponse<DrivingCharacterAnalysisResultDto>> getBedrock(@RequestBody DrivingCharacterAnalysisRequestDto requestDto){

        DrivingCharacterAnalysisResultDto result = bedrockService.invokeModel(requestDto);
        return ResponseEntity.ok(ApiResponse.success("Success for Bedrock Test.", result));
    }
}
