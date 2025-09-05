package com.smooth.driving_analysis_service.reports.accident_reaction.support;

/**
 * 주행 이벤트 타입 정의
 */
public enum DrivingEventType {
    HARD_BRAKE("hard_brake"),
    LANE_CHANGE("lane_change"), 
    SHARP_TURN("sharp_turn"),
    RAPID_ACCEL("rapid_accel");
    
    private final String eventName;
    
    DrivingEventType(String eventName) {
        this.eventName = eventName;
    }
    
    public String getEventName() {
        return eventName;
    }
    
    /**
     * 문자열로부터 DrivingEventType 찾기
     */
    public static DrivingEventType fromEventName(String eventName) {
        for (DrivingEventType type : values()) {
            if (type.eventName.equals(eventName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown event type: " + eventName);
    }
    
    /**
     * 문자열로부터 DrivingEventType 찾기 (별칭)
     */
    public static DrivingEventType of(String eventName) {
        return fromEventName(eventName);
    }
}