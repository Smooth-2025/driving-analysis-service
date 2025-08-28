package com.smooth.driving_analysis_service.driving.service;

import com.smooth.driving_analysis_service.driving.dto.request.DrivingCompletionRequestDto;
import com.smooth.driving_analysis_service.driving.dto.response.DrivingRecordResponseDto;
import com.smooth.driving_analysis_service.driving.dto.response.TodayDrivingResponseDto;

import java.util.concurrent.CompletableFuture;

public interface DrivingService {

    CompletableFuture<DrivingRecordResponseDto> summarize(DrivingCompletionRequestDto requestDto);

    TodayDrivingResponseDto getTodayDriving(Long userId);
}
