package com.smooth.driving_analysis_service.batch.service;

public interface StatusTransitionService {
    void toCompleted(Long reportId);
}
