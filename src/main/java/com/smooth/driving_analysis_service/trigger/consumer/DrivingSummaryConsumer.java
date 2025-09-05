package com.smooth.driving_analysis_service.trigger.consumer;

import java.util.Map;

import com.smooth.driving_analysis_service.trigger.dto.DrivingSummaryV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.smooth.driving_analysis_service.trigger.service.DrivingSummaryConsumerService;

@Component
@RequiredArgsConstructor
@Slf4j
public class DrivingSummaryConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private final StringRedisTemplate redis;
    private final DrivingSummaryConsumerService service;

    @Value("${app.stream.key:driving-analysis-stream}")
    private String streamKey;

    @Value("${app.stream.group:driving-analyzer-group}")
    private String groupName;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        try {
            Map<String, String> m = message.getValue();

            DrivingSummaryV1 dto = new DrivingSummaryV1();
            Integer vValue = parseInt(m.get("v"));
            dto.setV(vValue != null ? vValue : 1);
            dto.setUserId(m.get("userId"));
            dto.setDrivingId(m.get("drivingId"));
            dto.setEndedAt(m.get("endedAt"));
            dto.setStatus(m.get("status"));
            dto.setProducer(m.get("producer"));

            // DrivingRecord 필드와 매핑
            dto.setDrivingMinutes(parseInt(m.get("durationS")));
            dto.setTotalDistance(parseInt(m.get("distanceM")));
            dto.setAvgSpeed(parseDouble(m.get("avgSpeed")));
            // maxSpeed and minSpeed methods not available in DrivingSummaryV1
            dto.setCruiseRatio(parseDouble(m.get("cruiseRatio")));
            dto.setLaneChangeCount(parseInt(m.get("evLaneChange")));
            dto.setHardBrakeCount(parseInt(m.get("evHardBrake")));
            dto.setRapidAccelCount(parseInt(m.get("evRapidAccel")));
            // sharpTurnCount method not available in DrivingSummaryV1

            // 서비스 호출
            service.processDrivingSummary(message.getId().getValue(), dto);

            // ACK
            ack(message);
        } catch (Exception e) {
            log.error("[STREAM] processing failed id={} msg={} err={}",
                    message.getId().getValue(), message.getValue(), e.toString(), e);
        }
    }

    private void ack(MapRecord<String, String, String> message) {
        redis.opsForStream().acknowledge(streamKey, groupName, message.getId());
    }

    private Integer parseInt(String v) {
        if (v == null || v.isBlank()) return null;
        try {
            return Integer.valueOf(v);
        } catch (NumberFormatException e) {
            // Try parsing as double first, then convert to int
            try {
                return Double.valueOf(v).intValue();
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    private Long parseLong(String v) {
        if (v == null || v.isBlank()) return null;
        try {
            return Long.valueOf(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDouble(String v) {
        if (v == null || v.isBlank()) return null;
        try {
            return Double.valueOf(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}