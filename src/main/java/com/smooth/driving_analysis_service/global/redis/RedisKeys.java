package com.smooth.driving_analysis_service.global.redis;

/**
 * Redis 키 패턴 관리 유틸리티
 */
public class RedisKeys {
    
    // 멱등성 키 (TTL 7일)
    private static final String PROCESSED_TRIP_PREFIX = "processed:trip:";
    
    // 활성 리포트 캐시 (TTL 30일)
    private static final String ACTIVE_REPORT_PREFIX = "active-report:";
    
    // 스트림 키
    public static final String DRIVING_ANALYSIS_STREAM = "driving-analysis-stream";
    public static final String REPORT_TRIGGER_STREAM = "report.trigger";
    
    /**
     * 처리된 트립 키 생성
     * @param drivingId 주행 ID
     * @return processed:trip:{drivingId}
     */
    public static String processedTrip(String drivingId) {
        return PROCESSED_TRIP_PREFIX + drivingId;
    }
    
    /**
     * 사용자별 활성 리포트 키 생성
     * @param userId 사용자 ID
     * @return active-report:{userId}
     */
    public static String activeReportForUser(String userId) {
        return ACTIVE_REPORT_PREFIX + userId;
    }
    
    /**
     * 사용자별 활성 리포트 키 생성 (Long 타입)
     * @param userId 사용자 ID
     * @return active-report:{userId}
     */
    public static String activeReportForUser(Long userId) {
        return ACTIVE_REPORT_PREFIX + userId;
    }
}