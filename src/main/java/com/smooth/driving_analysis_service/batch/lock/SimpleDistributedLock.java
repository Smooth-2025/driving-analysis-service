package com.smooth.driving_analysis_service.batch.lock;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class SimpleDistributedLock {

    private final StringRedisTemplate redis;

    /** ttlSeconds 동안 점유 (초 단위) */
    public boolean acquire(String key, int ttlSeconds) {
        Boolean ok = redis.opsForValue().setIfAbsent(
                key, "1", Duration.ofSeconds(ttlSeconds)
        );
        return Boolean.TRUE.equals(ok);
    }

    public void release(String key) {
        try { redis.delete(key); } catch (Exception ignored) {}
    }
}
