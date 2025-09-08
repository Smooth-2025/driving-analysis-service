package com.smooth.driving_analysis_service.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.StreamOperations;
import software.amazon.awssdk.services.athena.AthenaClient;

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@TestConfiguration
public class TestAwsConfig {

    @Bean
    @Primary
    public AthenaClient athenaClient() {
        return mock(AthenaClient.class);
    }

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        return mock(RedisConnectionFactory.class);
    }

    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> template = mock(RedisTemplate.class);
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        
        when(template.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn(null);
        doNothing().when(valueOps).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        when(template.delete(anyString())).thenReturn(true);
        
        return template;
    }

    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        StreamOperations<String, Object, Object> streamOps = mock(StreamOperations.class);
        
        when(template.opsForValue()).thenReturn(valueOps);
        when(template.opsForStream()).thenReturn(streamOps);
        when(valueOps.get(anyString())).thenReturn(null);
        doNothing().when(valueOps).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        when(template.delete(anyString())).thenReturn(true);
        
        return template;
    }

    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public RedisTemplate<String, Object> redisTemplateObject() {
        return mock(RedisTemplate.class);
    }

    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public StreamOperations<String, Object, Object> streamOperations() {
        return mock(StreamOperations.class);
    }

    // Redis를 사용하는 서비스들도 mock 처리
    @Bean
    @Primary
    public com.smooth.driving_analysis_service.global.redis.service.RedisStreamService redisStreamService() {
        return mock(com.smooth.driving_analysis_service.global.redis.service.RedisStreamService.class);
    }

    @Bean
    @Primary
    public com.smooth.driving_analysis_service.batch.lock.SimpleDistributedLock simpleDistributedLock() {
        return mock(com.smooth.driving_analysis_service.batch.lock.SimpleDistributedLock.class);
    }

    @Bean
    @Primary
    public com.smooth.driving_analysis_service.batch.scheduler.NightlyBatchScheduler nightlyBatchScheduler() {
        return mock(com.smooth.driving_analysis_service.batch.scheduler.NightlyBatchScheduler.class);
    }

    // 추가 서비스들 mock 처리
    @Bean
    @Primary
    public com.smooth.driving_analysis_service.batch.service.NightlyBatchService nightlyBatchService() {
        return mock(com.smooth.driving_analysis_service.batch.service.NightlyBatchService.class);
    }

    @Bean
    @Primary
    public com.smooth.driving_analysis_service.batch.service.BatchWatermarkService batchWatermarkService() {
        return mock(com.smooth.driving_analysis_service.batch.service.BatchWatermarkService.class);
    }

    @Bean
    @Primary
    public com.smooth.driving_analysis_service.pipeline.service.DrivingIntegrationService drivingIntegrationService() {
        return mock(com.smooth.driving_analysis_service.pipeline.service.DrivingIntegrationService.class);
    }

    @Bean
    @Primary
    public com.smooth.driving_analysis_service.reports.milestone.service.MilestoneService milestoneService() {
        return mock(com.smooth.driving_analysis_service.reports.milestone.service.MilestoneService.class);
    }



    // 추가 서비스 구현체들
    @Bean
    @Primary
    public com.smooth.driving_analysis_service.driving.service.AthenaQueryService athenaQueryService() {
        return mock(com.smooth.driving_analysis_service.driving.service.AthenaQueryService.class);
    }

    @Bean
    @Primary
    public com.smooth.driving_analysis_service.driving.service.DrivingService drivingService() {
        return mock(com.smooth.driving_analysis_service.driving.service.DrivingService.class);
    }

    @Bean
    @Primary
    public com.smooth.driving_analysis_service.timeline.service.TimeLineService timeLineService() {
        return mock(com.smooth.driving_analysis_service.timeline.service.TimeLineService.class);
    }

    // DataSource 설정 (H2 인메모리 데이터베이스)
    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        config.setUsername("sa");
        config.setPassword("");
        return new HikariDataSource(config);
    }
}