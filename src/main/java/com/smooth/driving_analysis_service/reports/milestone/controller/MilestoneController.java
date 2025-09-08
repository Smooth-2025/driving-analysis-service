package com.smooth.driving_analysis_service.reports.milestone.controller;

import com.smooth.driving_analysis_service.global.auth.AuthenticationUtils;
import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.reports.milestone.dto.response.MilestoneReportResponse;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.service.MilestoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/driving-analysis/reports")
public class MilestoneController {

    private final MilestoneService milestoneService;


    /** 사용자별 마일스톤 목록 (카드 리스트) */
    @GetMapping
    public ApiResponse<List<MilestoneReportResponse>> list() {
        Long userId = AuthenticationUtils.getCurrentUserIdOrThrow();
        return ApiResponse.success("마일스톤 목록을 조회했습니다.", milestoneService.listByUser(userId));
    }

    /** 스탬프 개수 단독 조회 */
    @GetMapping("/stamp")
    public ApiResponse<MilestoneReportResponse> getStamp() {
        log.info("스탬프 조회 요청 시작");
        
        try {
            Long userId = AuthenticationUtils.getCurrentUserIdOrThrow();
            log.info("인증된 사용자 ID: {}", userId);
            
            MilestoneReportResponse response = milestoneService.getStampByUserId(userId);
            log.info("스탬프 조회 성공: {}", response);
            
            return ApiResponse.success("스탬프 정보를 조회했습니다.", response);
        } catch (Exception e) {
            log.error("스탬프 조회 중 오류 발생", e);
            throw e;
        }
    }

    /** 읽음 상태 설정 */
    @PatchMapping("/{reportId}/read")
    public ApiResponse<MilestoneReportResponse> setRead(@PathVariable String reportId) {
        // 인증 확인 (사용자 권한 체크용)
        AuthenticationUtils.getCurrentUserIdOrThrow();
        
        MilestoneReport updatedReport = milestoneService.updateReadByReportId(reportId, true);
        MilestoneReportResponse response = MilestoneReportResponse.from(updatedReport);
                
        return ApiResponse.success("읽음 상태가 업데이트되었습니다.", response);
    }
}
