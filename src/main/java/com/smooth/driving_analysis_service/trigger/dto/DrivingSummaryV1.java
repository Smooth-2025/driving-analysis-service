package com.smooth.driving_analysis_service.trigger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrivingSummaryV1 {

    private int v;
    private String userId;
    private String drivingId;

    private String startedAt;  // ISO 문자열로 변경
    private String endedAt;    // ISO 문자열로 변경
    private String status;
    private String producer;

    // XADD 스트림 필드들 (요구사항 기반)
    private Integer drivingMinutes;
    private Integer totalDistance;     // 미터 단위, Integer로 변경
    private Integer laneChangeCount;
    private Integer hardBrakeCount;
    private Integer rapidAccelCount;

    // DrivingRecord에서 가져올 추가 필드들 (pipeline에서 통합)
    private Double avgSpeed;
    private Double cruiseRatio;

    public boolean isCompleted() {
        return "COMPLETED".equalsIgnoreCase(status);
    }

    public boolean hasRequiredFields() {
        return userId != null && drivingId != null && endedAt != null && status != null;
    }

    public void validateForProcessing() {
        if (!hasRequiredFields()) {
            throw new IllegalArgumentException("Missing required fields");
        }
        if (!isCompleted()) {
            throw new IllegalArgumentException("Only COMPLETED trips can be processed");
        }
    }

    /**
     * startedAt 문자열을 LocalDateTime으로 변환
     */
    public LocalDateTime getStartedAtAsDateTime() {
        if (startedAt == null) return null;
        try {
            return LocalDateTime.parse(startedAt);
        } catch (Exception e) {
            // ISO 형식이 아닌 경우 타임스탬프로 시도
            try {
                long timestamp = Long.parseLong(startedAt);
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
            } catch (Exception ex) {
                return null;
            }
        }
    }

    /**
     * endedAt 문자열을 LocalDateTime으로 변환
     */
    public LocalDateTime getEndedAtAsDateTime() {
        if (endedAt == null) return null;
        try {
            return LocalDateTime.parse(endedAt);
        } catch (Exception e) {
            // ISO 형식이 아닌 경우 타임스탬프로 시도
            try {
                long timestamp = Long.parseLong(endedAt);
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
