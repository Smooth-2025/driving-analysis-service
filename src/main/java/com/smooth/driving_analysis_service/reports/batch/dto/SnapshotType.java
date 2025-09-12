package com.smooth.driving_analysis_service.reports.batch.dto;

public enum SnapshotType {
    INTERIM,  // 중간 스냅샷 (덮어쓰기 가능)
    FINAL     // 최종 스냅샷 (1회 확정)
}