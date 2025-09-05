package com.smooth.driving_analysis_service.reports.accident_reaction.controller;

import com.smooth.driving_analysis_service.reports.accident_reaction.dto.request.AccidentReactionRequestDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.AccidentReactionResponseDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.result.AccidentReactionResultDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.service.AccidentReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/driving-analysis/reports/accident-reaction")
@RequiredArgsConstructor
public class AccidentReactionController {

    private final AccidentReactionService service;

    @PostMapping("/alerts/{alertId}/rendered")
    public ResponseEntity<?> rendered(
            @PathVariable String alertId,
            @RequestParam long userId,
            @RequestBody AccidentReactionRequestDto req
    ) {
        AccidentReactionResponseDto data = service.recordRendered(alertId, userId, req);
        return ResponseEntity.ok(Map.of(
                "success", true, "code", 200, "message", "알림 렌더 수신", "data", data
        ));
    }

    @GetMapping("/summary")
    public ResponseEntity<?> summary(
            @RequestParam long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        AccidentReactionResultDto data = service.summary(userId, from, to);
        return ResponseEntity.ok(Map.of(
                "success", true, "code", 200, "message", "사고 알림 반응 요약", "data", data
        ));
    }
}
