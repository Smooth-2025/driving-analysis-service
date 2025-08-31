package com.smooth.driving_analysis_service.reports.milestone.controller;

import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.reports.milestone.service.MilestoneMaterializeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/debug/milestone")
@RequiredArgsConstructor
public class MilestoneMaterializeDebugController {

    private final MilestoneMaterializeService service;

    @PostMapping("/materialize")
    public ApiResponse<MilestoneMaterializeService.MaterializeResult> materialize(@RequestBody Req req) {
        var result = service.materialize(req.getUserId());
        return ApiResponse.success("요청성공", result);
    }

    @Data
    public static class Req {
        private Long userId;
        // 필요하면 cycleNo 도 추가
        // private Integer cycleNo;
    }
}
