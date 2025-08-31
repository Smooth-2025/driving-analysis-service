package com.smooth.driving_analysis_service.driving.service;

import com.smooth.driving_analysis_service.driving.dto.request.DrivingCompletionRequestDto;
import com.smooth.driving_analysis_service.timeline.dto.DrivingRecordResponseDto;
import com.smooth.driving_analysis_service.driving.dto.response.TodayDrivingResponseDto;
import com.smooth.driving_analysis_service.driving.dto.response.WeeklyDrivingResponseDto;

import java.util.concurrent.CompletableFuture;

public interface DrivingService {

    void summarize(DrivingCompletionRequestDto requestDto);

    TodayDrivingResponseDto getTodayDriving(Long userId);

    WeeklyDrivingResponseDto getWeeklyDriving(Long userId);
}
