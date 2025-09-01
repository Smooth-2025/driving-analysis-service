package com.smooth.driving_analysis_service.reports.milestone.batch.controller;

import com.smooth.driving_analysis_service.reports.milestone.batch.service.NightlyBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@RequestMapping("/admin/batch/milestones")
@RequiredArgsConstructor
public class BatchAdminController {

    private final NightlyBatchService nightlyBatchService;

    @PostMapping("/run-now")
    public ResponseEntity<String> runNow() {
        int processed = nightlyBatchService.runNightly(LocalDate.now(ZoneId.of("Asia/Seoul")));
        return ResponseEntity.ok("nightly started, processed=" + processed);
    }
}
