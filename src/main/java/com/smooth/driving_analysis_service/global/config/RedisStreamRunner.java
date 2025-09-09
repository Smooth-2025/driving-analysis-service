// src/main/java/com/smooth/driving_analysis_service/global/config/RedisStreamRunner.java
package com.smooth.driving_analysis_service.global.config;

import com.smooth.driving_analysis_service.reports.trigger.consumer.DrivingSummaryConsumer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

@Configuration
@Profile("!test")
public class RedisStreamRunner {

    private static final String STREAM_KEY = "driving-analysis-stream";
    private static final String GROUP      = "driving-analyzer-group";
    private static final String CONSUMER   = "worker-1";

    @Bean
    ApplicationRunner streamRunner(
            StringRedisTemplate redis,
            @Qualifier("streamContainer") StreamMessageListenerContainer<String, MapRecord<String, String, String>> container,
            DrivingSummaryConsumer drivingSummaryConsumer
    ) {
        return args -> {
            try {
                redis.opsForStream().createGroup(STREAM_KEY, ReadOffset.latest(), GROUP);
            } catch (Exception ignore) { }

            container.receive(
                    Consumer.from(GROUP, CONSUMER),
                    StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed()),
                    drivingSummaryConsumer
            );

            container.start();
        };
    }
}