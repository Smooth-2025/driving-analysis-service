package com.smooth.driving_analysis_service.progress.controller;

import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.progress.dto.ProgressDto;
import com.smooth.driving_analysis_service.progress.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    // T7.1.1 진행도 (n/15)
    @GetMapping("/progress")
    public ApiResponse<ProgressDto> progress(@RequestParam String userId) {
        var data = progressService.getProgress(userId.trim());
        return ApiResponse.success("진행도 조회 성공", data);
    }


    /** 숫자만 들어오면 "user{n}"로, 이미 user-prefix면 소문자 user로 통일 */
    private String normalizeUserId(String raw) {
        if (raw == null) return null;
        String v = raw.trim();

        // 숫자만 들어온 경우:  "9" -> "user9"
        if (v.matches("\\d+")) return "user" + v;

        // 이미 user 접두어면 접두어만 소문자로 통일: "USER9" / "User9" -> "user9"
        if (v.toLowerCase().startsWith("user")) {
            return "user" + v.substring(4); // "user" 이후 그대로 유지
        }

        // 그 외는 원본 유지
        return v;
    }
}
