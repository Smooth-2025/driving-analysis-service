package com.smooth.driving_analysis_service.reports.trigger.consumer;

import java.util.Map;

import com.smooth.driving_analysis_service.reports.trigger.dto.DrivingSummaryV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.smooth.driving_analysis_service.reports.trigger.service.DrivingSummaryConsumerService;
import org.springframework.context.annotation.Profile;

@Component
@Profile("!test")
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
            log.info("=== Redis Stream Message Debug ===");
            log.info("Stream Key: {}", message.getStream());
            log.info("Message ID: {}", message.getId());
            log.info("All Fields: {}", message.getValue());

            Map<String, String> m = message.getValue();

            // 각 필드별 상세 로깅 (camelCase와 snake_case 모두 확인)
            log.info("=== Field Mapping Debug ===");
            log.info("userId: {}", m.get("userId"));
            log.info("drivingId: {}", m.get("drivingId"));
            log.info("status: {}", m.get("status"));
            log.info("drivingMinutes (camel): {}, driving_minutes (snake): {}", m.get("drivingMinutes"), m.get("driving_minutes"));
            log.info("totalDistance (camel): {}, total_distance (snake): {}", m.get("totalDistance"), m.get("total_distance"));
            log.info("avgSpeed (camel): {}, avg_speed (snake): {}", m.get("avgSpeed"), m.get("avg_speed"));
            log.info("cruiseRatio (camel): {}, cruise_ratio (snake): {}", m.get("cruiseRatio"), m.get("cruise_ratio"));
            log.info("laneChangeCount (camel): {}, lane_change_count (snake): {}", m.get("laneChangeCount"), m.get("lane_change_count"));
            log.info("hardBrakeCount (camel): {}, hard_brake_count (snake): {}", m.get("hardBrakeCount"), m.get("hard_brake_count"));
            log.info("rapidAccelCount (camel): {}, rapid_accel_count (snake): {}", m.get("rapidAccelCount"), m.get("rapid_accel_count"));

            // 모든 키 출력
            log.info("Available keys: {}", m.keySet());

            DrivingSummaryV1 dto = new DrivingSummaryV1();
            Integer vValue = parseInt(m.get("v"));
            dto.setV(vValue != null ? vValue : 1);
            dto.setUserId(m.get("userId"));
            dto.setDrivingId(m.get("drivingId"));
            dto.setEndedAt(m.get("endedAt"));
            dto.setStatus(m.get("status"));
            dto.setProducer(m.get("producer"));

            // 실제 Redis Stream 필드명으로 매핑 (snake_case 또는 camelCase 모두 지원)
            dto.setDrivingMinutes(getIntValue(m, "drivingMinutes", "driving_minutes"));
            dto.setTotalDistance(getIntValue(m, "totalDistance", "total_distance"));
            dto.setAvgSpeed(getDoubleValue(m, "avgSpeed", "avg_speed"));
            dto.setCruiseRatio(getDoubleValue(m, "cruiseRatio", "cruise_ratio"));
            dto.setLaneChangeCount(getIntValue(m, "laneChangeCount", "lane_change_count"));
            dto.setHardBrakeCount(getIntValue(m, "hardBrakeCount", "hard_brake_count"));
            dto.setRapidAccelCount(getIntValue(m, "rapidAccelCount", "rapid_accel_count"));

            // 파싱된 결과 로깅
            log.info("=== Parsed DTO Debug ===");
            log.info("Parsed drivingMinutes: {}", dto.getDrivingMinutes());
            log.info("Parsed totalDistance: {}", dto.getTotalDistance());
            log.info("Parsed avgSpeed: {}", dto.getAvgSpeed());
            log.info("Parsed cruiseRatio: {}", dto.getCruiseRatio());
            log.info("Parsed laneChangeCount: {}", dto.getLaneChangeCount());
            log.info("Parsed hardBrakeCount: {}", dto.getHardBrakeCount());
            log.info("Parsed rapidAccelCount: {}", dto.getRapidAccelCount());

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
        if (v == null || v.isBlank())
            return null;
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



    private Double parseDouble(String v) {
        if (v == null || v.isBlank())
            return null;
        try {
            return Double.valueOf(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer getIntValue(Map<String, String> map, String camelKey, String snakeKey) {
        String value = map.get(camelKey);
        if (value == null) {
            value = map.get(snakeKey);
        }
        return parseInt(value);
    }

    private Double getDoubleValue(Map<String, String> map, String camelKey, String snakeKey) {
        String value = map.get(camelKey);
        if (value == null) {
            value = map.get(snakeKey);
        }
        return parseDouble(value);
    }
}