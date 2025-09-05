package com.smooth.driving_analysis_service.batch.service.impl;

import com.smooth.driving_analysis_service.batch.service.FinalAnalyzerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * TODO:
 *  - dna / v2x_response_score 포함 완성 스냅샷 생성
 *  - snapshot_type='FINAL' 기준 upsert
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FinalAnalyzerServiceImpl implements FinalAnalyzerService {

    @Override
    @Transactional
    public void buildFinalSnapshots(Long reportId) {
        log.info("[BATCH][FINAL] build snapshots for reportId={}", reportId);
        // ex) metricsWriter.upsertFinal(reportId);
        //     timePatternWriter.upsertFinal(reportId);
        //     eventAggWriter.upsertFinal(reportId);
        //     v2xWriter.upsertFinal(reportId);
        //     dnaWriter.upsertFinal(reportId);
    }
}
