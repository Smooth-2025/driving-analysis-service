package com.smooth.driving_analysis_service.reports.accident_reaction.support;

public enum DrivingEventType {
    RAPID_ACCEL("rapid_accel"),
    HARD_BRAKE("hard_brake"),
    LANE_CHANGE("lane_change"),
    SHARP_TURN("sharp_turn");

    private final String json;
    DrivingEventType(String json) { this.json = json; }
    public static DrivingEventType of(String s) {
        if (s == null) return null;
        String k = s.trim().toLowerCase();
        for (var t : values()) if (t.json.equals(k)) return t;
        return null;
    }
}
