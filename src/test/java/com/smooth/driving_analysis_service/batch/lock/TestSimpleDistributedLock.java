package com.smooth.driving_analysis_service.batch.lock;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@Profile("test")
public class TestSimpleDistributedLock extends SimpleDistributedLock {

    private final ConcurrentMap<String, Boolean> locks = new ConcurrentHashMap<>();

    public TestSimpleDistributedLock() {
        super(null); // StringRedisTemplate은 테스트에서 사용하지 않음
    }

    @Override
    public boolean acquire(String key, int ttlSeconds) {
        return locks.putIfAbsent(key, true) == null;
    }

    @Override
    public void release(String key) {
        locks.remove(key);
    }
}