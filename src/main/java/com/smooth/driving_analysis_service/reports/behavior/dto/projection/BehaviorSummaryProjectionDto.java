package com.smooth.driving_analysis_service.reports.behavior.dto.projection;

/**
 * Behavior Summary Projection for native queries
 */
public interface BehaviorSummaryProjectionDto {
    Integer getHardBrakeCount();
    Integer getRapidAccelCount();
    Integer getLaneChangeCount();
}