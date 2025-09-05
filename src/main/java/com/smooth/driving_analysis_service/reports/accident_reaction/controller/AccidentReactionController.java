package com.smooth.driving_analysis_service.reports.accident_reaction.controller;

import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.reports.accident_reaction.service.AccidentReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/driving-analysis/reports")
public class AccidentReactionController {

    private final AccidentReactionService svc;

    @GetMapping("/{reportId}/accident-reaction")
    public ApiResponse<Map<String, Object>> get(@PathVariable Long reportId) {
        return ApiResponse.success("OK", svc.buildAccidentResponse(reportId));
    }
}
