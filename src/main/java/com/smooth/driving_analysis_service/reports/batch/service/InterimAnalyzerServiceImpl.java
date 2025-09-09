package com.smooth.driving_analysis_service.reports.batch.service;

import com.smooth.driving_analysis_service.reports.basic_summary.service.BasicSummaryService;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * INTERIM 스냅샷 생성 서비스
 * - basic_summary 테이블에 INTERIM 스냅샷 생성
 * - 추후 다른 리포트 테이블들도 추가 예정
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InterimAnalyzerServiceImpl implements InterimAnalyzerService {

    private final BasicSummaryService basicSummaryService;
    private final MilestoneReportRepository milestoneReportRepository;

    @Override
    @Transactional
    public void upsertInterimSnapshots(Long reportId) {
        log.info("[BATCH][INTERIM] upsert snapshots for reportId={}", reportId);
        
        try {
            // 마일스톤 리포트 조회하여 userId 획득
            var milestoneReport = milestoneReportRepository.findById(reportId)
                    .orElseThrow(() -> new RuntimeException("MilestoneReport not found: " + reportId));
            
            // BasicSummary INTERIM 스냅샷 생성
            basicSummaryService.generateInterimReport(reportId, milestoneReport.getUserId());
            
            log.info("[BATCH][INTERIM] snapshots created successfully for reportId={}", reportId);
            
        } catch (Exception e) {
            log.error("[BATCH][INTERIM] failed to create snapshots for reportId={}", reportId, e);
            throw e;
        }
    }
}
