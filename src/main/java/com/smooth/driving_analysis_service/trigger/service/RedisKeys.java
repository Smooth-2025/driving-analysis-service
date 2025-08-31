package com.smooth.driving_analysis_service.trigger.service;

public final class RedisKeys {
    private RedisKeys() {}
    public static String userCycleTrips(long userId) { return "user:cycle:trips:" + userId; }
    public static String userCycleLock(long userId) { return "user:cycle:lock:" + userId; }
}
