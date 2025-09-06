package com.smooth.driving_analysis_service.batch.consumer;

import com.smooth.driving_analysis_service.batch.dto.ReportTriggerV1;
import com.smooth.driving_analysis_service.batch.service.BatchReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * report.trigger 스트림을 소비하여 배치 리포트 처리를 수행하는 컨슈머
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class ReportTriggerConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private final StringRedisTemplate redisTemplate;
    private final BatchReportService batchReportService;

    @Value("${app.batch.stream.key:report.trigger}")
    private String streamKey;

    @Value("${app.batch.stream.group:batch-report-group}")
    private String groupName;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        String messageId = message.getId().getValue();
        Map<String, String> fields = message.getValue();
        
        log.info("Received report trigger: messageId={}, fields={}", messageId, fields);
        
        try {
            // 메시지 파싱
            ReportTriggerV1 trigger = parseMessage(fields);
            
            log.info("Processing report trigger: type={}, userId={}, reportId={}, milestone={}, drivingCount={}", 
                    trigger.getType(), trigger.getUserId(), trigger.getReportId(), 
                    trigger.getMilestone(), trigger.getDrivingIds().size());

            // 배치 리포트 처리
            batchReportService.processReportTrigger(trigger);

            // 메시지 ACK
            acknowledgeMessage(message);
            
            log.info("Report trigger processed successfully: messageId={}, reportId={}", 
                    messageId, trigger.getReportId());
            
        } catch (Exception e) {
            log.error("Failed to process report trigger: messageId={}, fields={}", 
                    messageId, fields, e);
            // TODO: 실패한 메시지 처리 (DLQ, 재시도 등)
        }
    }

    /**
     * Redis 스트림 메시지를 ReportTriggerV1 객체로 파싱
     */
    private ReportTriggerV1 parseMessage(Map<String, String> fields) {
        return ReportTriggerV1.builder()
                .v(parseInteger(fields.get("v"), 1))
                .type(fields.get("type"))
                .userId(fields.get("userId"))
                .reportId(parseLong(fields.get("reportId")))
                .milestone(parseInteger(fields.get("milestone"), null))
                .status(fields.get("status"))
                .drivingIds(parseDrivingIds(fields.get("drivingIds")))
                .emittedAt(parseDateTime(fields.get("emittedAt")))
                .producer(fields.get("producer"))
                .traceId(fields.get("traceId"))
                .build();
    }

    /**
     * 메시지 ACK 처리
     */
    private void acknowledgeMessage(MapRecord<String, String, String> message) {
        try {
            redisTemplate.opsForStream().acknowledge(streamKey, groupName, message.getId());
        } catch (Exception e) {
            log.warn("Failed to acknowledge message: messageId={}", message.getId().getValue(), e);
        }
    }

    // 파싱 유틸리티 메서드들
    private Long parseLong(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse long value: {}", value);
            return null;
        }
    }

    private Integer parseInteger(String value, Integer defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse integer value: {}", value);
            return defaultValue;
        }
    }

    private List<String> parseDrivingIds(String value) {
        if (value == null || value.isBlank()) return Collections.emptyList();
        try {
            return Arrays.asList(value.split(","));
        } catch (Exception e) {
            log.warn("Failed to parse driving IDs: {}", value);
            return Collections.emptyList();
        }
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value);
        } catch (Exception e) {
            log.warn("Failed to parse datetime value: {}", value);
            return null;
        }
    }
}