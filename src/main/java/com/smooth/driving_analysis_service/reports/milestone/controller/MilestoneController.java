package com.smooth.driving_analysis_service.reports.milestone.controller;

import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.reports.milestone.dto.request.*;
import com.smooth.driving_analysis_service.reports.milestone.dto.response.MilestoneReportResponse;
import com.smooth.driving_analysis_service.reports.milestone.service.MilestoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/driving-analysis/reports")
public class MilestoneController {

    private final MilestoneService milestoneService;


    /** 사용자별 마일스톤 목록 (카드 리스트) */
    @GetMapping
    public ApiResponse<List<MilestoneReportResponse>> list(@RequestParam long userId) {
        return ApiResponse.success("마일스톤 목록을 조회했습니다.", milestoneService.listByUser(userId));
    }

    /** 스탬프 개수 단독 조회 */
    @GetMapping("/{id}/stamp")
    public ApiResponse<MilestoneReportResponse> getStamp(@PathVariable long id) {
        return ApiResponse.success("스탬프 정보를 조회했습니다.", milestoneService.getStamp(id));
    }

    /** 읽음 상태 설정 */
    @PatchMapping("/{id}/read")
    public ApiResponse<Void> setRead(
            @PathVariable long id,
            @RequestBody @Valid MilestoneReadToggleRequest req
    ) {
        milestoneService.updateRead(id, req.read());
        return ApiResponse.success("읽음 상태가 업데이트되었습니다.");
    }
}
