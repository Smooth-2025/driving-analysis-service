// reports/accident_reaction/controller/AccidentResponseController.java
package com.smooth.driving_analysis_service.reports.accident_reaction.controller;

import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.reports.accident_reaction.service.AccidentResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/driving-analysis/reports")
public class AccidentResponseController {

    private final AccidentResponseService svc;

    @GetMapping("/{reportId}/accident-response")
    public ApiResponse<Map<String, Object>> get(@PathVariable Long reportId) {
        return ApiResponse.success(svc.buildAccidentResponse(reportId));
    }
}
