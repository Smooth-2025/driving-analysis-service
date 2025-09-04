package com.smooth.driving_analysis_service.batch.consumer;

import com.smooth.driving_analysis_service.batch.dto.ReportTriggerV1;
import com.smooth.driving_analysis_service.batch.service.BatchReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            
            log.info("트리거 처리 시작 - type: {}, userId: {}, reportId: {}, milestone: {}", 
                    trigger.getType(), trigger.getUserId(), trigger.getReportId(), trigger.getMilestone());

            batchReportService.processReportTrigger(trigger);

            // ACK 처리
            redis.opsForStream().acknowledge("report-trigger-group", message);
            
            log.info("트리거 처리 완료 - reportId: {}", trigger.getReportId());
            
        } catch (Exception e) {
            log.error("ReportTrigger 메시지 처리 실패: {}", message.getId(), e);
            // 에러 처리 로직 (재시도, DLQ 등)
        }
    }
    
    private List<String> parseDrivingIds(String drivingIdsStr) {
        if (drivingIdsStr == null || drivingIdsStr.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.asList(drivingIdsStr.split(","));
    }
}