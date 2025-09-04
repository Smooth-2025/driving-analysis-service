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

import java.util.Arrays;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportTriggerConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private final StringRedisTemplate redis;
    private final BatchReportService batchReportService;

    @Value("${app.batch.stream.key:report-trigger}")
    private String streamKey;

    @Value("${app.batch.stream.group:batch-report-group}")
    private String groupName;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        try {
            Map<String, String> m = message.getValue();
            
            ReportTriggerV1 trigger = ReportTriggerV1.builder()
                    .type(m.get("type"))
                    .userId(m.get("userId"))
                    .reportId(parseLong(m.get("reportId")))
                    .milestone(parseInt(m.get("milestone")))
                    .status(m.get("status"))
                    .drivingIds(parseDrivingIds(m.get("tripIds"))) // Redis에서는 tripIds로 전송됨
                    .build();

            log.info("[BATCH] Processing report trigger: type={}, userId={}, reportId={}, milestone={}", 
                    trigger.getType(), trigger.getUserId(), trigger.getReportId(), trigger.getMilestone());

            batchReportService.processReportTrigger(trigger);

            // ACK
            ack(message);
            
        } catch (Exception e) {
            log.error("[BATCH] Failed to process report trigger: id={}, msg={}, err={}", 
                    message.getId().getValue(), message.getValue(), e.toString(), e);
        }
    }

    private void ack(MapRecord<String, String, String> message) {
        redis.opsForStream().acknowledge(streamKey, groupName, message.getId());
    }

    private Long parseLong(String v) {
        if (v == null || v.isBlank()) return null;
        try {
            return Long.valueOf(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInt(String v) {
        if (v == null || v.isBlank()) return null;
        try {
            return Integer.valueOf(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private java.util.List<String> parseDrivingIds(String v) {
        if (v == null || v.isBlank()) return java.util.Collections.emptyList();
        return Arrays.asList(v.split(","));
    }
}