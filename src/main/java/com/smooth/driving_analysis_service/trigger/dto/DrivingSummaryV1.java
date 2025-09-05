package com.smooth.driving_analysis_service.trigger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrivingSummaryV1 {

    private int v;
    private String userId;
    private String drivingId;

    private Long startedAt;
    private Long endedAt;
    private String status;
    private String producer;

    // 👉 DrivingEventDto 기반으로 확장된 필드들
    private Integer drivingMinutes;
    private Integer totalDistance;
    private Integer laneChangeCount;
    private Integer hardBrakeCount;
    private Integer rapidAccelCount;

    public boolean isCompleted() {
        return "COMPLETED".equalsIgnoreCase(status);
    }

    public boolean hasRequiredFields() {
        return userId != null && drivingId != null && endedAt != null && endedAt > 0 && status != null;
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
