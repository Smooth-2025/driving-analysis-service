package com.smooth.driving_analysis_service.config;

import com.smooth.driving_analysis_service.driving.service.AthenaQueryService;
import com.smooth.driving_analysis_service.global.redis.service.RedisStreamService;
import com.smooth.driving_analysis_service.trigger.producer.ReportTriggerProducer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 테스트용 설정 클래스
 * 외부 서비스들(AWS, Redis 등)을 Mock으로 처리하여 순수한 비즈니스 로직만 테스트
 */
@TestConfiguration
public class TestConfig {

    /**
     * AWS Athena 서비스 Mock
     * 실제 AWS 호출 없이 테스트 가능
     */
    @MockBean
    private AthenaQueryService athenaQueryService;

    /**
     * Redis Stream 서비스 Mock
     * 실제 Redis 서버 없이 테스트 가능
     */
    @MockBean
    private RedisStreamService redisStreamService;

    /**
     * Redis Template Mock
     * 실제 Redis 연결 없이 테스트 가능
     */
    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 리포트 트리거 프로듀서 Mock
     * 실제 메시지 큐 없이 테스트 가능
     */
    @MockBean
    private ReportTriggerProducer reportTriggerProducer;
}