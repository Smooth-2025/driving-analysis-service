package com.smooth.driving_analysis_service.batch.service;

import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class NightlyBatchService {

    private final ReportFinderService reportFinder;       // 대상 리포트 조회/아이템 수 집계
    private final InterimAnalyzerService interimAnalyzer; // Interim 스냅샷 upsert
    private final FinalAnalyzerService finalAnalyzer;     // Final 스냅샷 upsert
    private final StatusTransitionService statusTransition;
    private final BatchWatermarkService watermark;

    /** dev(10분 주기)에서도 안전하게 재실행 가능한 idempotent 구조 */
    @Transactional
    public void runOnce(LocalDate asOf) {

        // 1) COLLECTING 중 N>=4 → Interim 스냅샷 upsert
        for (var report : reportFinder.findReports(MilestoneReport.Status.COLLECTING)) {
            int n = reportFinder.countItems(report.getId());
            if (n >= 4) {
                interimAnalyzer.upsertInterimSnapshots(report.getId());
            }
        }

        // 2) PROCESSING(=15개 채움) → Final 생성 + COMPLETED 전환
        for (var report : reportFinder.findReports(MilestoneReport.Status.PROCESSING)) {
            finalAnalyzer.buildFinalSnapshots(report.getId());
            statusTransition.toCompleted(report.getId());
        }

        watermark.markProcessed(asOf);
        log.info("[BATCH] finished runOnce for {}", asOf);
    }
}
