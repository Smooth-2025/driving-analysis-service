package com.smooth.driving_analysis_service.progress.controller;

import com.smooth.driving_analysis_service.progress.dto.EligibilityDto;
import com.smooth.driving_analysis_service.progress.service.EligibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor

public class EligibilityController {

    private final EligibilityService service;

    /** T7.1.2 - 15회 달성 여부 조회 */
    @GetMapping("/eligibility")
    public ResponseEntity<EligibilityDto> getEligibility(@RequestParam String userId) {
        return ResponseEntity.ok(service.getEligibility(userId));
    }
}
