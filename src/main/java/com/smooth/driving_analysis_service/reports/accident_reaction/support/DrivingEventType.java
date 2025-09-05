package com.smooth.driving_analysis_service.reports.accident_reaction.support;

public enum DrivingEventType {
    COLLISION,
    NEAR_MISS,
    HARD_BRAKE,
    SUDDEN_ACCELERATION,
    LANE_DEPARTURE,
    UNKNOWN;

    public static DrivingEventType of(String eventType) {
        if (eventType == null || eventType.isBlank()) {
            return UNKNOWN;
        }
        
        try {
            return valueOf(eventType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}