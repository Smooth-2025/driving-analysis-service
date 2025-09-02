package com.smooth.driving_analysis_service.global.redis;

public final class RedisKeys {
    private RedisKeys() {}

    // 🚗 Trip 처리 관련
    public static String processedTrip(String drivingId) { return "processed:trip:" + drivingId; }

    // 📊 사용자 Cycle 관리
    public static String userCycleTrips(long userId) { return "user:cycle:trips:" + userId; }
    public static String userCycleLock(long userId) { return "user:cycle:lock:" + userId; }

    // 📝 리포트 상태
    public static String activeReportForUser(String userId) { return "user:active-report:" + userId; }
}