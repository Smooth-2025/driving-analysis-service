package com.smooth.driving_analysis_service.trigger.producer;

import com.smooth.driving_analysis_service.batch.dto.ReportTriggerV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * report.trigger 스트림에 배치 트리거를 발행하는 프로듀서
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportTriggerProducer {

    private final RedisTemplate<String, String> redisTemplate;
    
    private static final String STREAM_KEY = "report.trigger";
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * 리포트 트리거를 report.trigger 스트림에 발행
     */
    public RecordId emit(ReportTriggerV1 trigger) {
        Map<String, String> fields = buildStreamFields(trigger);
        
        RecordId recordId = redisTemplate.opsForStream().add(STREAM_KEY, fields);
        
        log.info("Report trigger emitted: streamId={}, type={}, reportId={}, userId={}, milestone={}", 
                recordId.getValue(), trigger.getType(), trigger.getReportId(), 
                trigger.getUserId(), trigger.getMilestone());
        
        return recordId;
    }

    /**
     * ReportTriggerV1을 Redis 스트림 필드로 변환
     */
    private Map<String, String> buildStreamFields(ReportTriggerV1 trigger) {
        Map<String, String> fields = new LinkedHashMap<>();
        
        fields.put("v", String.valueOf(trigger.getV()));
        fields.put("type", trigger.getType());
        fields.put("userId", trigger.getUserId());
        fields.put("reportId", String.valueOf(trigger.getReportId()));
        fields.put("milestone", String.valueOf(trigger.getMilestone()));
        fields.put("status", trigger.getStatus());
        
        // drivingIds를 쉼표로 구분된 문자열로 변환
        if (trigger.getDrivingIds() != null && !trigger.getDrivingIds().isEmpty()) {
            fields.put("drivingIds", String.join(",", trigger.getDrivingIds()));
        } else {
            fields.put("drivingIds", "");
        }
        
        // 메타데이터
        if (trigger.getEmittedAt() != null) {
            fields.put("emittedAt", trigger.getEmittedAt().format(DATETIME_FORMATTER));
        }
        if (trigger.getProducer() != null) {
            fields.put("producer", trigger.getProducer());
        }
        if (trigger.getTraceId() != null) {
            fields.put("traceId", trigger.getTraceId());
        }
        
        return fields;
    }
}