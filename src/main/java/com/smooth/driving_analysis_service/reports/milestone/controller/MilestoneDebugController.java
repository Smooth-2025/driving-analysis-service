package com.smooth.driving_analysis_service.reports.milestone.controller;

import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.driving.repository.DrivingRecordRepository;
import com.smooth.driving_analysis_service.reports.milestone.service.MilestoneTriggerService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
@Profile("dev")
@RestController
@RequiredArgsConstructor
public class MilestoneDebugController {

    private final MilestoneTriggerService milestoneTriggerService;
    private final DrivingRecordRepository drivingRecordRepository;

    // ✅ 이벤트 발행까지 포함 (T7.2.1 실제 동작 검증용)
    @PostMapping("/_debug/milestone/check")
    public ApiResponse<CheckResult> checkAndPublish(@RequestBody CheckRequest req) {
        long total = drivingRecordRepository.countByUserId(req.userId());
        boolean isMultiple = (total > 0 && total % 15 == 0);
        boolean reached = milestoneTriggerService.onTripCommitted(req.userId()); // 이벤트 발행
        return ApiResponse.success("milestone check (publish) 완료",
                new CheckResult(req.userId(), total, isMultiple, reached));
    }

    // 👀 이벤트 발행 없이 상태만 미리보기
    @GetMapping("/_debug/milestone/peek")
    public ApiResponse<PeekResult> peek(@RequestParam Long userId) {
        long total = drivingRecordRepository.countByUserId(userId);
        boolean isMultiple = (total > 0 && total % 15 == 0);
        return ApiResponse.success("milestone peek 완료",
                new PeekResult(userId, total, isMultiple));
    }

    // 요청/응답 DTO (record로 간단히)
    public record CheckRequest(Long userId) {}
    public record CheckResult(Long userId, long totalCount, boolean multipleOf15, boolean milestoneReached) {}
    public record PeekResult(Long userId, long totalCount, boolean multipleOf15) {}
}
