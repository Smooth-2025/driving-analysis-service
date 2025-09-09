package com.smooth.driving_analysis_service.reports.batch.service;

public interface StatusTransitionService {
    void toCompleted(Long reportId);
}
