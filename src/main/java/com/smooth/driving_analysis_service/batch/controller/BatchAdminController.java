package com.smooth.driving_analysis_service.batch.controller;

import com.smooth.driving_analysis_service.batch.scheduler.NightlyBatchScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/driving-analysis/internal/batch")
public class BatchAdminController {

    private final NightlyBatchScheduler scheduler;

    @PostMapping("/run")
    public ResponseEntity<?> run() {
        scheduler.run();
        return ResponseEntity.ok(Map.of("triggered", true));
    }
}
