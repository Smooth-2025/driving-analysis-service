package com.smooth.driving_analysis_service.progress.controller;

import com.smooth.driving_analysis_service.progress.dto.BannerDto;
import com.smooth.driving_analysis_service.progress.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/banner")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    // 배너 노출 여부/미확인 개수 조회
    @GetMapping
    public ResponseEntity<BannerDto> getFlag(@RequestParam String userId) {
        return ResponseEntity.ok(bannerService.getStatus(userId));
    }

    // 배너 '확인' 멱등 처리
    @PostMapping("/ack")
    public ResponseEntity<BannerDto> ack(@RequestParam String userId) {
        return ResponseEntity.ok(bannerService.ack(userId));
    }
}
