package com.smooth.driving_analysis_service.trigger.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DrivingSummaryV1 {
    private int v;
    private String userId;
    private String drivingId;
    private Long startedAt;
    private long endedAt;
    private String status;
    private String producer;

    private Integer durationS;   // duration_s
    private Integer distanceM;   // distance_m
    private Integer evHardBrake; // ev_hard_brake
    private Integer evRapidAccel;// ev_rapid_accel
    private Integer evLaneChange;// ev_lane_change

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public boolean hasRequiredFields() {
        return userId != null && drivingId != null && endedAt > 0 && status != null;
    }

    public void validateForProcessing() {
        if (!hasRequiredFields()) {
            throw new IllegalArgumentException("Missing required fields");
        }
        if (!isCompleted()) {
            throw new IllegalArgumentException("Only COMPLETED trips can be processed");
        }
    }
}
