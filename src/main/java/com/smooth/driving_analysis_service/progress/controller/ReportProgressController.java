package com.smooth.driving_analysis_service.progress.controller;

import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.progress.dto.ReportEligibilityDto;
import com.smooth.driving_analysis_service.progress.dto.ReportProgressDto;
import com.smooth.driving_analysis_service.progress.service.ReportProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportProgressController {

    private final ReportProgressService reportProgressService;

    /** 진행도 파악 */
    @GetMapping("/progress")
    public ApiResponse<ReportProgressDto> getProgress(@RequestParam("userId") String userId) {
        // 1) userId 정규화: "9" -> "user9", " user9 " -> "user9", "USER9" -> "user9

        ReportProgressDto dto = reportProgressService.getProgress(userId);

        String message = (dto.getTotalTrips() == 0)
                ? "아직 주행 기록이 없습니다. 15회 달성 시 리포트가 생성됩니다."
                : "진행도 조회가 완료되었습니다.";

        return ApiResponse.success(message, dto);
    }

    /** (신규) 달성 여부 */
    @GetMapping("/eligibility")
    public ApiResponse<ReportEligibilityDto> getEligibility(@RequestParam("userId") String userId) {
        ReportEligibilityDto dto = reportProgressService.getEligibility(userId);
        String message = dto.isEligible()
                ? "리포트 생성 조건(15회)을 달성했습니다."
                : "리포트 생성까지 " + dto.getRemainingTrips() + "회 남았습니다.";
        return ApiResponse.success(message, dto);
    }

}
