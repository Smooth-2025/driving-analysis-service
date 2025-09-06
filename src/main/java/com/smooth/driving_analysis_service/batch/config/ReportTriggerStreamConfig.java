package com.smooth.driving_analysis_service.batch.config;

import com.smooth.driving_analysis_service.batch.consumer.ReportTriggerConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import java.time.Duration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ReportTriggerStreamConfig {

    private final ReportTriggerConsumer reportTriggerConsumer;

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> reportTriggerStreamContainer(
            RedisConnectionFactory connectionFactory) {
        
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        .pollTimeout(Duration.ofSeconds(1))
                        .build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(connectionFactory, options);

        // report.trigger 스트림 구독
        Subscription subscription = container.receive(
                Consumer.from("batch-group", "batch-consumer-1"),
                StreamOffset.create("report.trigger", ReadOffset.lastConsumed()),
                reportTriggerConsumer
        );

        log.info("Report trigger stream subscription created: {}", subscription);
        
        container.start();
        return container;
    }
}