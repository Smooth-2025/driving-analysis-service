package com.smooth.driving_analysis_service.driving.controller;

import com.smooth.driving_analysis_service.driving.dto.request.DrivingCompletionRequestDto;
import com.smooth.driving_analysis_service.driving.dto.response.TodayDrivingResponseDto;
import com.smooth.driving_analysis_service.driving.dto.response.WeeklyDrivingResponseDto;
import com.smooth.driving_analysis_service.driving.service.DrivingService;
import com.smooth.driving_analysis_service.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/driving-analysis/summary")
@RestController
public class DrivingController {

    private final DrivingService drivingService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> summarizeDriving(
            @RequestBody @Valid DrivingCompletionRequestDto requestDto) {

        drivingService.summarize(requestDto);

        return ResponseEntity.ok(ApiResponse.success("주행 요약 처리가 시작되었습니다."));
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<TodayDrivingResponseDto>> getTodayDriving(
            // TODO: API GATEWAY에서 userId
    ) {
        TodayDrivingResponseDto responseDto = drivingService.getTodayDriving(1L);

        return ResponseEntity.ok(ApiResponse.success("오늘의 주행 통계 조회가 완료되었습니다.", responseDto));
    }

    @GetMapping("/weekly")
    public ResponseEntity<ApiResponse<WeeklyDrivingResponseDto>> getWeeklyDriving(
            // TODO: API GATEWAY에서 userId
    ) {
        WeeklyDrivingResponseDto responseDto = drivingService.getWeeklyDriving(1L);

        return ResponseEntity.ok(ApiResponse.success("최근 7일 주행 통계 조회가 완료되었습니다.", responseDto));
    }

}
