package com.smooth.driving_analysis_service.batch.consumer;

import com.smooth.driving_analysis_service.batch.service.BatchReportService;
import com.smooth.driving_analysis_service.trigger.dto.ReportTriggerV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportTriggerConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private final BatchReportService batchReportService;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        try {
            ReportTriggerV1 trigger = parseMessage(message);
            log.info("Received report trigger: type={}, reportId={}, milestone={}", 
                    trigger.getType(), trigger.getReportId(), trigger.getMilestone());
            
            batchReportService.processReportTrigger(trigger);
            
        } catch (Exception e) {
            log.error("Failed to process report trigger message: {}", message.getId(), e);
            // TODO: 실패한 메시지를 DLQ로 보내거나 재시도 로직 구현
        }
    }

    private ReportTriggerV1 parseMessage(MapRecord<String, String, String> message) {
        var fields = message.getValue();
        
        // tripIds 필드를 drivingIds로 변환 (Producer에서 tripIds로 보냄)
        String tripIdsStr = fields.get("tripIds");
        List<String> drivingIds = (tripIdsStr != null && !tripIdsStr.isEmpty()) 
                ? Arrays.asList(tripIdsStr.split(","))
                : List.of();
        
        return ReportTriggerV1.builder()
                .v(Integer.parseInt(fields.getOrDefault("v", "1")))
                .type(fields.get("type"))
                .userId(fields.get("userId"))
                .reportId(Long.parseLong(fields.get("reportId")))
                .milestone(Integer.parseInt(fields.get("milestone")))
                .drivingIds(drivingIds)
                .status(fields.get("status"))
                .emittedAt(parseDateTime(fields.get("emittedAt")))
                .producer(fields.get("producer"))
                .traceId(fields.get("traceId"))
                .build();
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null) return LocalDateTime.now();
        
        try {
            return LocalDateTime.parse(dateTimeStr, 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS"));
        } catch (Exception e) {
            log.warn("Failed to parse emittedAt: {}, using current time", dateTimeStr);
            return LocalDateTime.now();
        }
    }
}