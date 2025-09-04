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
    private Long endedAt;
    private String status;
    private String producer;

    // DrivingRecord 필드와 매핑되는 필드들
    private Integer drivingMinutes;
    private Double totalDistance;      // DrivingRecord와 타입 맞춤
    private Double avgSpeed;
    private Double maxSpeed;
    private Double minSpeed;
    private Double cruiseRatio;
    private Integer laneChangeCount;
    private Integer hardBrakeCount;
    private Integer rapidAccelCount;
    private Integer sharpTurnCount;    // 누락된 필드 추가

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
