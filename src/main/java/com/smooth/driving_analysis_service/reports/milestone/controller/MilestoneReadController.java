package com.smooth.driving_analysis_service.reports.milestone.controller;

import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.reports.milestone.service.MilestoneReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/driving-analysis/reports") // ✅ prefix 유지
public class MilestoneReadController {

    private final MilestoneReadService service;

    // 본문이 없거나 {"read": null} 이면 토글, {"read": true/false}면 설정
    @PatchMapping("/{id}/read")
    public ApiResponse<MilestoneReadService.ReadResult> patchRead(
            @PathVariable Long id,
            @RequestBody(required = false) ReadRequest req
    ) {
        if (req == null || req.read() == null) {
            var res = service.toggle(id);
            return ApiResponse.success("read toggled", res);
        } else {
            var res = service.set(id, req.read());
            return ApiResponse.success("read set", res);
        }
    }

    public record ReadRequest(Boolean read) {}
}
