// reports/dna/controller/DnaBatchController.java
package com.smooth.driving_analysis_service.reports.dna.controller;

import com.smooth.driving_analysis_service.global.common.ApiResponse;
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
    public ResponseEntity<ApiResponse<DnaSnapshot>> runInterim(@PathVariable Long reportId) {
        DnaSnapshot s = service.runInterim(reportId);
        return ResponseEntity.ok(ApiResponse.success("DNA 운전성향 INTERIM 분석이 성공했습니다.",s));
    }

    @PostMapping("/{reportId}/dna:final")
    public ResponseEntity<ApiResponse<DnaSnapshot>> runFinal(@PathVariable Long reportId) {
        DnaSnapshot s = service.runFinal(reportId);
        return ResponseEntity.ok(ApiResponse.success("DNA 운전성향 FINAL 분석이 성공했습니다.",s));
    }
}
