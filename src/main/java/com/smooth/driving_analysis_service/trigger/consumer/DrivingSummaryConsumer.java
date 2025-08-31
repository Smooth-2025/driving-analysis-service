package com.smooth.driving_analysis_service.trigger.consumer;

import com.smooth.driving_analysis_service.trigger.service.DrivingSummaryConsumerService;
import com.smooth.driving_analysis_service.trigger.util.DrivingSummaryParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DrivingSummaryConsumer {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisConnectionFactory connectionFactory;
    private final DrivingSummaryConsumerService consumerService;
    private final DrivingSummaryParser parser;

    // ===== 환경변수 우선(없으면 yaml 기본값) =====
    @Value("${DRIVING_STREAM_NAME:${redis.stream.driving.name:driving-analysis-stream}}")
    private String streamKey;

    @Value("${DRIVING_CONSUMER_GROUP:${redis.stream.driving.group:driving-analyzer-group}}")
    private String group;

    @Value("${DRIVING_CONSUMER_NAME:${redis.stream.driving.consumer:analyzer-worker-1}}")
    private String consumerName;

    // DLQ (유효성 실패 등 비재시도 메시지 목적지)
    @Value("${DRIVING_DLQ_NAME:${redis.stream.driving.dlq:driving-analysis-dlq}}")
    private String dlqKey;

    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    @PostConstruct
    public void start() {
        log.info("DrivingSummaryConsumer boot - stream='{}', group='{}', consumer='{}'",
                streamKey, group, consumerName);

        ensureStreamAndGroup();

        var options = StreamMessageListenerContainer
                .StreamMessageListenerContainerOptions
                .<String, MapRecord<String, String, String>>builder()
                .batchSize(10)
                .pollTimeout(Duration.ofSeconds(2))
                .build();

        container = StreamMessageListenerContainer.create(connectionFactory, options);

        container.receive(Consumer.from(group, consumerName),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                this::onMessage);

        container.start();
        log.info("DrivingSummaryConsumer started: stream={}, group={}, consumer={}",
                streamKey, group, consumerName);
    }

    private void ensureStreamAndGroup() {
        // 1) 스트림 없으면 dummy 레코드로 생성
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(streamKey))) {
            redisTemplate.opsForStream().add(streamKey, Map.of("init", "true"));
            log.info("Created stream {}", streamKey);
        }
        // 2) 그룹 생성 (이미 있으면 BUSYGROUP 무시)
        try {
            redisTemplate.opsForStream()
                    .createGroup(streamKey, ReadOffset.from("0-0"), group);
            log.info("Created consumer group {}", group);
        } catch (RedisSystemException e) {
            Throwable root = e;
            while (root.getCause() != null) root = root.getCause();
            String msg = String.valueOf(root.getMessage());
            if (msg.contains("BUSYGROUP")) {
                log.debug("Consumer group already exists: {}", group);
            } else {
                throw e;
            }
        }

        // DLQ 스트림도 없으면 만들어 둠(선택)
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(dlqKey))) {
            redisTemplate.opsForStream().add(dlqKey, Map.of("init", "true"));
            log.info("Created DLQ stream {}", dlqKey);
        }
    }

    private void onMessage(MapRecord<String, String, String> record) {
        String id = record.getId().getValue();
        Map<String, String> fields = record.getValue();

        try {
            // 유효성/필드 파싱
            var summary = parser.parse(fields);

            // 비즈니스 처리
            consumerService.processDrivingSummary(id, summary);

            // 성공 ACK
            redisTemplate.opsForStream().acknowledge(group, record);
            log.debug("ACK {}", id);

        } catch (IllegalArgumentException badInput) {
            // ❌ 유효성 실패/파싱 오류: 재시도 무의미 → DLQ로 보낸 뒤 원본 ACK
            sendToDlq(id, fields, "BAD_INPUT", badInput.getMessage());
            redisTemplate.opsForStream().acknowledge(group, record);
            log.warn("BAD_INPUT -> DLQ and ACK {}: {}", id, badInput.getMessage());

        } catch (Exception e) {
            // ❗ 재시도 가능(일시적 DB/네트워크 등) → 원본 미ACK(=pending 유지)
            // 컨테이너가 계속 poll 하므로 나중에 재처리되거나, 운영자가 PEL/CLAIM/재시작으로 복구
            log.error("Failed to process {}: {}", id, e.getMessage(), e);
            // 필요 시: 특정 횟수 이상 실패 시 DLQ로 스위치하고 ACK 처리하는 로직을 추가 가능
        }
    }

    private void sendToDlq(String id, Map<String, String> original, String errorType, String errorMessage) {
        Map<String, String> payload = new HashMap<>(original);
        payload.putIfAbsent("errorType", errorType);
        payload.put("errorMessage", String.valueOf(errorMessage));
        payload.put("originalId", id);
        payload.put("timestamp", String.valueOf(Instant.now().toEpochMilli()));
        redisTemplate.opsForStream().add(StreamRecords.newRecord()
                .in(dlqKey)
                .ofMap(payload));
    }

    @PreDestroy
    public void stop() {
        if (container != null) {
            container.stop();
            log.info("DrivingSummaryConsumer stopped");
        }
    }
}