package com.smooth.driving_analysis_service.batch.controller;

import com.smooth.driving_analysis_service.batch.scheduler.NightlyBatchScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Profile("!test")
@RequiredArgsConstructor
@RequestMapping("/api/driving-analysis/internal/batch")
@ConditionalOnProperty(name = "scheduling.enabled", havingValue = "true", matchIfMissing = false)
public class BatchAdminController {

    private final NightlyBatchScheduler scheduler;

    @PostMapping("/run")
    public ResponseEntity<?> run() {
        scheduler.run();
        return ResponseEntity.ok(Map.of("triggered", true));
    }
}