package com.smooth.driving_analysis_service.batch.controller;

import com.smooth.driving_analysis_service.batch.service.NightlyBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/driving-analysis/admin/batch/nightly")
@RequiredArgsConstructor
public class BatchAdminController {

    private final NightlyBatchService nightly;

    /** 전일 실행 (편의) */
    @PostMapping("/run-yesterday")
    public ResponseEntity<String> runYesterday(@RequestParam(defaultValue = "false") boolean force) {
        LocalDate y = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1);
        int processed = nightly.runManual(y, force);
        return ResponseEntity.ok("OK - yesterday=" + y + ", processed=" + processed + ", force=" + force);
    }

    /** 특정 일자 1회 실행 */
    @PostMapping("/run")
    public ResponseEntity<String> runOnce(
            @RequestParam LocalDate asOf,
            @RequestParam(defaultValue = "false") boolean force
    ) {
        int processed = nightly.runManual(asOf, force);
        return ResponseEntity.ok("OK - date=" + asOf + ", processed=" + processed + ", force=" + force);
    }

    /** asOf부터 days일 연속 실행 */
    @PostMapping("/run-range")
    public ResponseEntity<String> runRange(
            @RequestParam LocalDate asOf,
            @RequestParam(defaultValue = "1") int days,
            @RequestParam(defaultValue = "false") boolean force
    ) {
        int total = nightly.runRangeManual(asOf, days, force);
        return ResponseEntity.ok("OK - start=" + asOf + ", days=" + days + ", totalProcessed=" + total + ", force=" + force);
    }
}
