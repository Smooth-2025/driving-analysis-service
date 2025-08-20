package com.smooth.driving_analysis_service.progress.controller;

import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.progress.dto.ReportProgressDto;
import com.smooth.driving_analysis_service.progress.service.ReportProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportProgressController {

    private final ReportProgressService reportProgressService;

    @GetMapping("/progress")
    public ApiResponse<ReportProgressDto> getProgress(@RequestParam("userId") String userIdParam) {
        // 1) userId 정규화: "9" -> "user9", " user9 " -> "user9", "USER9" -> "user9"
        String userId = normalizeUserId(userIdParam);

        ReportProgressDto dto = reportProgressService.getProgress(userId);

        String message = (dto.getTotalTrips() == 0)
                ? "아직 주행 기록이 없습니다. 15회 달성 시 리포트가 생성됩니다."
                : "진행도 조회가 완료되었습니다.";

        return ApiResponse.success(message, dto);
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
