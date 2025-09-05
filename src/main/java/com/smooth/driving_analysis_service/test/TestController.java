package com.smooth.driving_analysis_service.test;

import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import com.smooth.driving_analysis_service.reports.milestone.service.MilestoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final MilestoneReportRepository milestoneReportRepository;
    private final MilestoneService milestoneService;

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("애플리케이션이 정상 동작 중입니다.", "OK");
    }

    @PostMapping("/milestone/create")
    public ApiResponse<MilestoneReport> createMilestone(@RequestParam Long userId) {
        log.info("Creating milestone for userId: {}", userId);
        
        // 새 마일스톤 리포트 생성
        MilestoneReport report = MilestoneReport.newCollecting(userId, 1);
        MilestoneReport saved = milestoneReportRepository.save(report);
        
        log.info("Created milestone report: id={}, reportId={}", saved.getId(), saved.getReportId());
        return ApiResponse.success("마일스톤 리포트가 생성되었습니다.", saved);
    }

    @GetMapping("/milestone/list/{userId}")
    public ApiResponse<List<MilestoneReport>> listMilestones(@PathVariable Long userId) {
        log.info("Listing milestones for userId: {}", userId);
        
        List<MilestoneReport> reports = milestoneReportRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        
        return ApiResponse.success("마일스톤 리포트 목록 조회 완료", reports);
    }

    @PostMapping("/milestone/complete/{reportId}")
    public ApiResponse<String> completeMilestone(@PathVariable Long reportId) {
        log.info("Completing milestone reportId: {}", reportId);
        
        try {
            milestoneService.markReportCompleted(reportId);
            return ApiResponse.success("마일스톤 리포트가 완료되었습니다.", "OK");
        } catch (Exception e) {
            log.error("Failed to complete milestone", e);
            return ApiResponse.success("마일스톤 완료 중 오류가 발생했습니다: " + e.getMessage(), "ERROR");
        }
    }

    @GetMapping("/db/tables")
    public ApiResponse<String> checkTables() {
        try {
            long count = milestoneReportRepository.count();
            return ApiResponse.success("데이터베이스 연결 정상, milestone_report 테이블 레코드 수: " + count, "OK");
        } catch (Exception e) {
            log.error("Database connection failed", e);
            return ApiResponse.success("데이터베이스 연결 실패: " + e.getMessage(), "ERROR");
        }
    }
}