package com.smooth.driving_analysis_service.reports.batch.service;

public interface InterimAnalyzerService {
    void upsertInterimSnapshots(Long reportId);
}
