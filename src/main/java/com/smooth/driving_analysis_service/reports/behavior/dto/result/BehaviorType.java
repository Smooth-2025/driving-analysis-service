package com.smooth.driving_analysis_service.reports.behavior.dto.result;

/**
 * 위험 행동 타입 Enum
 */
public enum BehaviorType {
    HARD_BRAKE("hardBrake", "급제동"),
    RAPID_ACCEL("rapidAccel", "급가속"),
    LANE_CHANGE("laneChange", "차선변경");
    
    private final String code;
    private final String description;
    
    BehaviorType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static BehaviorType from(String value) {
        for (BehaviorType type : values()) {
            if (type.code.equals(value) || type.name().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown BehaviorType: " + value);
    }
}