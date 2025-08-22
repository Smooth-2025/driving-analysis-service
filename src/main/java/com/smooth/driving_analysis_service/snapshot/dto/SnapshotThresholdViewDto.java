package com.smooth.driving_analysis_service.snapshot.dto;

public record SnapshotThresholdViewDto(
        int threshold,              // 15
        int currentCycleCount,      // 0..15
        int missingTrips,           // max(15 - current, 0)
        boolean willReachAfterNext, // current == 14
        boolean eligibleNow,        // current >= 15 (보통 즉시 발행되어 0으로 리셋됨)
        int cycleIndex              // 현재 사이클 번호
) {
    public static SnapshotThresholdViewDto of(int threshold, int current, int cycleIndex) {
        int missing = Math.max(threshold - current, 0);
        boolean willNext = (current == threshold - 1);
        boolean eligible = (current >= threshold);
        return new SnapshotThresholdViewDto(threshold, current, missing, willNext, eligible, cycleIndex);
    }
}
