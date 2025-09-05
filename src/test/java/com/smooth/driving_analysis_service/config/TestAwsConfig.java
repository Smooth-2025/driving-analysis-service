package com.smooth.driving_analysis_service.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import software.amazon.awssdk.services.athena.AthenaClient;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@TestConfiguration
@Profile("test")
public class TestAwsConfig {

    @Bean
    @Primary
    public AthenaClient athenaClient() {
        // 테스트 환경에서는 Mock AthenaClient 사용
        return mock(AthenaClient.class);
    }

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        // 테스트 환경에서는 Mock RedisConnectionFactory 사용
        return mock(RedisConnectionFactory.class);
    }

    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public RedisTemplate<String, String> redisTemplate() {
        // 테스트 환경에서는 Mock RedisTemplate 사용
        RedisTemplate<String, String> template = mock(RedisTemplate.class);
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        
        when(template.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn(null); // 기본적으로 캐시 미스
        doNothing().when(valueOps).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        doNothing().when(template).delete(anyString());
        
        return template;
    }
}