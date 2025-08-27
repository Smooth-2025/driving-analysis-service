package com.smooth.driving_analysis_service.reports.milestone.controller;

import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.reports.milestone.service.MilestoneMaterializeService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@Profile("dev") // 운영 차단 권장
@RestController
@RequestMapping("/api/driving-analysis/reports") // ✅ prefix 유지
@RequiredArgsConstructor
public class MilestoneMaterializeDebugController {

    private final MilestoneMaterializeService materializeService;

    @PostMapping("/_debug/milestone/materialize")
    public ApiResponse<MilestoneMaterializeService.MaterializeResult> materialize(@RequestBody Req req) {
        var res = materializeService.materializeLatest15(req.userId());
        return ApiResponse.success("materialize 완료", res);
    }

    public record Req(Long userId) {}
}
