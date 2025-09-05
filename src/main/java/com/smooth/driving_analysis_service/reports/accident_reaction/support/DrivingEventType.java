package com.smooth.driving_analysis_service.reports.accident_reaction.support;

public enum DrivingEventType {
    ACCIDENT_ALERT,
    COLLISION_WARNING,
    EMERGENCY_BRAKE,
    LANE_DEPARTURE;
    
    public static DrivingEventType of(String eventTypeStr) {
        if (eventTypeStr == null) return ACCIDENT_ALERT;
        
        return switch (eventTypeStr.toUpperCase()) {
            case "COLLISION_WARNING" -> COLLISION_WARNING;
            case "EMERGENCY_BRAKE" -> EMERGENCY_BRAKE;
            case "LANE_DEPARTURE" -> LANE_DEPARTURE;
            default -> ACCIDENT_ALERT;
        };
    }
}