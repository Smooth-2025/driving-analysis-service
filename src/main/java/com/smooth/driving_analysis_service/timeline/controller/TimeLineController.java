package com.smooth.driving_analysis_service.timeline.controller;

import com.smooth.driving_analysis_service.timeline.dto.TimeLineResponseDto;
import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.timeline.service.TimeLineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/driving-analysis/timeline")
@RestController
public class TimeLineController {

    private final TimeLineService timeLineService;

    @GetMapping("/driving")
    public ResponseEntity<ApiResponse<TimeLineResponseDto>> getDrivingTimeLine(
            // @RequestHeader("X-User-Id) Long userId
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int limit) {
        TimeLineResponseDto responseDto = timeLineService.getDrivingTimeLine(1L, cursor, limit);

        return ResponseEntity.ok(ApiResponse.success("주행 타임라인 조회가 완료되었습니다.", responseDto));
    }

}
