package com.smooth.driving_analysis_service.batch.service;

public interface InterimAnalyzerService {
    void upsertInterimSnapshots(Long reportId);
}
