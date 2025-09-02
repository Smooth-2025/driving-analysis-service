package com.smooth.driving_analysis_service.pipeline;

import com.smooth.driving_analysis_service.trigger.dto.DrivingSummaryV1;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/debug/pipeline")
public class DebugPipelineController {
    private final RealtimeDrivingService realtimeDrivingService;

    @PostMapping("/apply")
    public ResponseEntity<?> apply(@RequestBody DrivingSummaryV1 body) {
        try {
            realtimeDrivingService.applySummary(body);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            // 개발환경에서는 원인 로그
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                    Map.of("ok", false, "error", e.getClass().getSimpleName(), "message", e.getMessage())
            );
        }
    }
}
