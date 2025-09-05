package com.smooth.driving_analysis_service.reports.behavior.entity;

public enum BehaviorType {
    HARD_BRAKE,     // 급제동
    RAPID_ACCEL,    // 급가속
    LANE_CHANGE;     // 차선변경

    public static BehaviorType from(String s) {
        return BehaviorType.valueOf(s.toUpperCase());
    }
}
