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
import java.util.List;
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
            log.info("ReportTrigger 메시지 수신: {}", message.getId());
            
            Map<String, String> m = message.getValue();
            
            ReportTriggerV1 trigger = ReportTriggerV1.builder()
                    .v(Integer.parseInt(m.getOrDefault("v", "1")))
                    .type(m.get("type"))
                    .userId(m.get("userId"))
                    .reportId(m.get("reportId"))
                    .milestone(m.get("milestone"))
                    .status(m.get("status"))
                    .drivingIds(parseDrivingIds(m.get("drivingIds")))
                    .build();
            
            log.info("[BATCH] Processing report trigger: type={}, userId={}, reportId={}, milestone={}", 
                    trigger.getType(), trigger.getUserId(), trigger.getReportId(), trigger.getMilestone());

            batchReportService.processReportTrigger(trigger);

            // ACK
            ack(message);
            
            log.info("트리거 처리 완료 - reportId: {}", trigger.getReportId());
            
        } catch (Exception e) {
            log.error("[BATCH] Failed to process report trigger: id={}, msg={}, err={}", 
                    message.getId().getValue(), message.getValue(), e.toString(), e);
        }
    }

    private void ack(MapRecord<String, String, String> message) {
        redis.opsForStream().acknowledge(streamKey, groupName, message.getId());
    }

    private List<String> parseDrivingIds(String drivingIdsStr) {
        if (drivingIdsStr == null || drivingIdsStr.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.asList(drivingIdsStr.split(","));
    }
}