package com.smooth.driving_analysis_service.reports.accident_reaction.support;

public enum DrivingEventType {
    HARD_BRAKE("hard_brake"),
    LANE_CHANGE("lane_change"),
    SHARP_TURN("sharp_turn"),
    RAPID_ACCEL("rapid_accel");

    private final String value;

    DrivingEventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DrivingEventType fromValue(String value) {
        for (DrivingEventType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown DrivingEventType: " + value);
    }
    
    public static DrivingEventType of(String value) {
        return fromValue(value);
    }
}