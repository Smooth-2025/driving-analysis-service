package com.smooth.driving_analysis_service.trigger;

public class RedisKeys {
    public static String processedTrip(String drivingId) { return "processed:trip:" + drivingId; }
    public static String activeReportForUser(String userId) { return "user:active-report:" + userId; }
}
