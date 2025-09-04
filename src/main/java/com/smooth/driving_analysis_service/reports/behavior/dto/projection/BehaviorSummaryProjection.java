package com.smooth.driving_analysis_service.reports.behavior.dto.projection;

public interface BehaviorSummaryProjection {
    Integer getHardBrakeCount();
    Integer getRapidAccelCount();
    Integer getLaneChangeCount();
    
    default Integer getTotal() {
        return (getHardBrakeCount() != null ? getHardBrakeCount() : 0) +
               (getRapidAccelCount() != null ? getRapidAccelCount() : 0) +
               (getLaneChangeCount() != null ? getLaneChangeCount() : 0);
    }
}