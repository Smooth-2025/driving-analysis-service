// reports/dna/controller/DnaController.java
package com.smooth.driving_analysis_service.reports.dna.controller;

import com.smooth.driving_analysis_service.reports.dna.dto.DnaResponseDto;
import com.smooth.driving_analysis_service.reports.dna.entity.DnaSnapshot;
import com.smooth.driving_analysis_service.reports.dna.repository.DnaSnapshotRepository;
import com.smooth.driving_analysis_service.reports.dna.service.DnaComputeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/driving-analysis/reports")
public class DnaController {

    private final DnaSnapshotRepository repo;
    private final DnaComputeService compute;

    @GetMapping("/{reportId}/dna")
    public ResponseEntity<?> getDna(@PathVariable Long reportId) {
        DnaSnapshot snap = repo.findByReportId(reportId)
                .orElseThrow(() -> new NoSuchElementException("DNA not ready for report " + reportId));

        String[] parts = snap.getCode().split("-");
        String a = parts[0]; String b = parts[1]; String c = parts[2]; String d = parts[3];

        var radar = Map.of("A", snap.getScoreA(), "B", snap.getScoreB(), "C", snap.getScoreC(), "D", snap.getScoreD());

        var meta = compute.axisMeta(a, b, c, d);
        List<DnaResponseDto.AxisCard> axes = List.of(
                DnaResponseDto.AxisCard.builder().id("A").label(meta.get("A")[0]).summary(meta.get("A")[1]).build(),
                DnaResponseDto.AxisCard.builder().id("B").label(meta.get("B")[0]).summary(meta.get("B")[1]).build(),
                DnaResponseDto.AxisCard.builder().id("C").label(meta.get("C")[0]).summary(meta.get("C")[1]).build(),
                DnaResponseDto.AxisCard.builder().id("D").label(meta.get("D")[0]).summary(meta.get("D")[1]).build()
        );

        var data = DnaResponseDto.builder()
                .code(snap.getCode())
                .headline(snap.getHeadline())
                .radar(radar)
                .axes(axes)
                .build();

        return ResponseEntity.ok(Map.of("success", true, "code", "SUCCESS", "message", "ok", "data", data));
    }
}
