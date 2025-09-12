package com.smooth.driving_analysis_service.reports.milestone.controller;

import com.smooth.driving_analysis_service.global.auth.AuthenticationUtils;
import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.reports.milestone.dto.response.MilestoneReportResponseDto;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.service.MilestoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/driving-analysis/milestone")
@RequiredArgsConstructor
@Slf4j
public class MilestoneController {

    private final MilestoneService milestoneService;

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<MilestoneReportResponseDto>>> list() {
        Long userId = AuthenticationUtils.getCurrentUserIdOrThrow();
        return ResponseEntity.ok(ApiResponse.success("마일스톤 목록을 조회했습니다.", milestoneService.listByUser(userId)));
    }

    @GetMapping("/stamp")
    public ResponseEntity<ApiResponse<MilestoneReportResponseDto>> getStamp() {
        try {
            Long userId = AuthenticationUtils.getCurrentUserIdOrThrow();
            log.info("인증된 사용자 ID: {}", userId);
            
            MilestoneReportResponseDto response = milestoneService.getStampByUserId(userId);
            log.info("스탬프 조회 성공: {}", response);
            
            return ResponseEntity.ok(ApiResponse.success("스탬프를 조회했습니다.", response));
        } catch (Exception e) {
            log.error("스탬프 조회 중 오류 발생", e);
            throw e;
        }
    }

    @PutMapping("/{reportId}/read")
    public ResponseEntity<ApiResponse<MilestoneReportResponseDto>> markAsRead(@PathVariable String reportId) {
        AuthenticationUtils.getCurrentUserIdOrThrow();
        
        MilestoneReport updatedReport = milestoneService.updateReadByReportId(reportId, true);
        MilestoneReportResponseDto response = MilestoneReportResponseDto.from(updatedReport);
        
        return ResponseEntity.ok(ApiResponse.success("마일스톤을 읽음 처리했습니다.", response));
    }
}