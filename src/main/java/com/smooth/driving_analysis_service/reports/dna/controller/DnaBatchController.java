// reports/dna/controller/DnaBatchController.java
package com.smooth.driving_analysis_service.reports.dna.controller;

import com.smooth.driving_analysis_service.reports.dna.entity.DnaSnapshot;
import com.smooth.driving_analysis_service.reports.dna.service.DnaBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/driving-analysis/reports")
public class DnaBatchController {

    private final DnaBatchService service;

    @PostMapping("/{reportId}/dna:interim")
    public ResponseEntity<?> runInterim(@PathVariable Long reportId) {
        DnaSnapshot s = service.runInterim(reportId);
        return ResponseEntity.ok(Map.of("success", true, "code", "SUCCESS", "message", "interim OK", "data", s.getCode()));
    }

    @PostMapping("/{reportId}/dna:final")
    public ResponseEntity<?> runFinal(@PathVariable Long reportId) {
        DnaSnapshot s = service.runFinal(reportId);
        return ResponseEntity.ok(Map.of("success", true, "code", "SUCCESS", "message", "final OK", "data", s.getCode()));
    }
}
