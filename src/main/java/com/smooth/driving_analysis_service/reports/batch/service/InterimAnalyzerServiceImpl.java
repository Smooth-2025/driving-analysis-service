package com.smooth.driving_analysis_service.reports.batch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * TODO:
 *  - report_metrics / time_pattern_bins / trip_event_agg 등
 *  - snapshot_type='INTERIM' 기준으로 upsert
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InterimAnalyzerServiceImpl implements InterimAnalyzerService {

    @Override
    @Transactional
    public void upsertInterimSnapshots(Long reportId) {
        // 형님 프로젝트의 실제 쿼리/서비스 호출 포인트 연결
        log.info("[BATCH][INTERIM] upsert snapshots for reportId={}", reportId);
        // ex) metricsWriter.upsertInterim(reportId);
        //     timePatternWriter.upsertInterim(reportId);
        //     eventAggWriter.upsertInterim(reportId);
    }
}
