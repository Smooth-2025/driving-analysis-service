package com.smooth.driving_analysis_service;

import com.smooth.driving_analysis_service.batch.scheduler.NightlyBatchScheduler;
import com.smooth.driving_analysis_service.reports.dna.service.DnaBatchService;
import com.smooth.driving_analysis_service.trigger.producer.ReportTriggerProducer;
import com.smooth.driving_analysis_service.global.redis.service.RedisStreamService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

// ★ AthenaClient import 추가
import software.amazon.awssdk.services.athena.AthenaClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.task.scheduling.enabled=false",
        "scheduling.cron.current=0 0 3 * * *",
        "aws.region=us-east-1",
        "aws.s3.bucket=test-bucket",
        "spring.redis.host=localhost",
        "spring.redis.port=6379"
})
class DrivingAnalysisServiceApplicationTests {

    @TestConfiguration
    static class Mocks {
        @Bean NightlyBatchScheduler nightlyBatchScheduler() { return Mockito.mock(NightlyBatchScheduler.class); }
        @Bean ReportTriggerProducer reportTriggerProducer() { return Mockito.mock(ReportTriggerProducer.class); }
        @Bean RedisStreamService redisStreamService() { return Mockito.mock(RedisStreamService.class); }

        // ★ 여기 추가: AthenaClient 목
        @Bean AthenaClient athenaClient() { return Mockito.mock(AthenaClient.class); }
        @Bean DnaBatchService dnaBatchService() { return Mockito.mock(DnaBatchService.class); }
    @Test
    void contextLoads() {}
    }
}
