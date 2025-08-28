package com.smooth.driving_analysis_service.driving.controller;

import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.driving.dto.request.DrivingCompletionRequestDto;
import com.smooth.driving_analysis_service.driving.dto.response.DrivingRecordResponseDto;
import com.smooth.driving_analysis_service.driving.service.DrivingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@RequestMapping("/api/driving-analysis/summary")
@RestController
public class DrivingController {

    private final DrivingService drivingService;

    @PostMapping
    public ResponseEntity<ApiResponse<CompletableFuture<DrivingRecordResponseDto>>> summarizeDriving(
            @RequestBody @Valid DrivingCompletionRequestDto requestDto) {

        CompletableFuture<DrivingRecordResponseDto> responseDto = drivingService.summarize(requestDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("주행 요약 처리가 시작되었습니다.", responseDto));
    }

}
