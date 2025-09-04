package com.smooth.driving_analysis_service.timeline.controller;


import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.timeline.dto.TimeLineResponseDto;
import com.smooth.driving_analysis_service.timeline.service.TimeLineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RequestMapping("/api/driving-analysis/timeline")
@RestController
public class TimeLineController {


    private final TimeLineService timeLineService;


    // 기존: /driving
    @GetMapping("/driving")
    public ResponseEntity<ApiResponse<TimeLineResponseDto>> getDrivingTimeLine(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int limit) {
        TimeLineResponseDto dto = timeLineService.getDrivingTimeLine(1L, cursor, limit); // TODO: X-User-Id 헤더로 교체
        return ResponseEntity.ok(ApiResponse.success("주행 타임라인 조회가 완료되었습니다.", dto));
    }


    // 전체 타임라인 (주행 + 리포트)
    @GetMapping
    public ResponseEntity<ApiResponse<TimeLineResponseDto>> getTimeLine(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int limit) {
        TimeLineResponseDto dto = timeLineService.getAllTimeLine(1L, cursor, limit);
        return ResponseEntity.ok(ApiResponse.success("타임라인 조회가 완료되었습니다.", dto));
    }

    // 리포트만 타임라인
    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<TimeLineResponseDto>> getReportsTimeLine(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int limit) {
        TimeLineResponseDto dto = timeLineService.getReportTimeLine(1L, cursor, limit);
        return ResponseEntity.ok(ApiResponse.success("리포트 타임라인 조회가 완료되었습니다.", dto));
    }
}