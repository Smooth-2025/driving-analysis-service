package com.smooth.driving_analysis_service.timeline.controller;


import com.smooth.driving_analysis_service.global.auth.AuthenticationUtils;
import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.timeline.dto.TimeLineResponseDto;
import com.smooth.driving_analysis_service.timeline.service.TimeLineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
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
        Long userId = AuthenticationUtils.getCurrentUserIdOrThrow();
        TimeLineResponseDto dto = timeLineService.getDrivingTimeLine(userId, cursor, limit);
        return ResponseEntity.ok(ApiResponse.success("주행 타임라인 조회가 완료되었습니다.", dto));
    }


    // 신규: /report
    @GetMapping("/report")
    public ResponseEntity<ApiResponse<TimeLineResponseDto>> getReportTimeLine(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = AuthenticationUtils.getCurrentUserIdOrThrow();
        TimeLineResponseDto dto = timeLineService.getReportTimeLine(userId, cursor, limit);
        return ResponseEntity.ok(ApiResponse.success("리포트 타임라인 조회가 완료되었습니다.", dto));
    }


    // 신규: /all
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<TimeLineResponseDto>> getAllTimeLine(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = AuthenticationUtils.getCurrentUserIdOrThrow();
        log.info("전체 타임라인 API 호출 - userId: {}, cursor: {}, limit: {}", userId, cursor, limit);
        
        TimeLineResponseDto dto = timeLineService.getAllTimeLine(userId, cursor, limit);
        log.info("전체 타임라인 API 응답 - items: {}, hasMore: {}", 
                dto.getItems().size(), dto.isHasMore());
        
        return ResponseEntity.ok(ApiResponse.success("전체 타임라인 조회가 완료되었습니다.", dto));
    }
}