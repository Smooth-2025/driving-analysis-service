package com.smooth.driving_analysis_service.reports.batch.service;

import com.smooth.driving_analysis_service.reports.basic_summary.service.BasicSummaryService;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * FINAL 스냅샷 생성 서비스
 * - basic_summary 테이블에 FINAL 스냅샷 생성
 * - 추후 dna, v2x_response_score 등 다른 리포트 테이블들도 추가 예정
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FinalAnalyzerServiceImpl implements FinalAnalyzerService {

    private final BasicSummaryService basicSummaryService;
    private final MilestoneReportRepository milestoneReportRepository;

    @Override
    @Transactional
    public void buildFinalSnapshots(Long reportId) {
        log.info("[BATCH][FINAL] build snapshots for reportId={}", reportId);
        
        try {
            // 마일스톤 리포트 조회하여 userId 획득
            var milestoneReport = milestoneReportRepository.findById(reportId)
                    .orElseThrow(() -> new RuntimeException("MilestoneReport not found: " + reportId));
            
            // BasicSummary FINAL 스냅샷 생성
            basicSummaryService.generateFinalReport(reportId, milestoneReport.getUserId());
            
            // TODO: 추후 다른 리포트들도 추가
            // dnaService.generateFinalReport(reportId, milestoneReport.getUserId());
            // v2xService.generateFinalReport(reportId, milestoneReport.getUserId());
            
            log.info("[BATCH][FINAL] snapshots created successfully for reportId={}", reportId);
            
        } catch (Exception e) {
            log.error("[BATCH][FINAL] failed to create snapshots for reportId={}", reportId, e);
            throw e;
        }
    }
}
