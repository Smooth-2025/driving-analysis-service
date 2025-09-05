package com.smooth.driving_analysis_service.global.redis.service;

import com.smooth.driving_analysis_service.driving.exception.DrivingErrorCode;
import com.smooth.driving_analysis_service.global.exception.BusinessException;
import com.smooth.driving_analysis_service.global.redis.dto.DrivingEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisStreamServiceImpl implements RedisStreamService {

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${app.stream.driving-name:driving-analysis-stream}")
    private String drivingStreamName;

    @Override
    public void publishDrivingEvent(DrivingEventDto e) {
        try {
            // 컨슈머가 사용하는 필드명과 맞춰서 전송(endedAt vs endTime 중 하나로 통일)
            Map<String, String> m = new LinkedHashMap<>();
            m.put("v", String.valueOf(e.getV()));
            m.put("userId", e.getUserId().toString());
            m.put("drivingId", e.getDrivingId());
            m.put("endedAt", e.getEndTime().toString());
            m.put("status", e.getStatus());
            m.put("drivingMinutes", String.valueOf(e.getDrivingMinutes()));
            m.put("totalDistance", String.valueOf(e.getTotalDistance()));
            m.put("laneChangeCount", String.valueOf(e.getLaneChangeCount()));
            m.put("hardBrakeCount", String.valueOf(e.getHardBrakeCount()));
            m.put("rapidAccelCount", String.valueOf(e.getRapidAccelCount()));

            RecordId id = stringRedisTemplate.opsForStream()
                    .add(MapRecord.create(drivingStreamName, m));

            log.info("주행 이벤트 발행 완료: stream={}, id={}, drivingId={}",
                    drivingStreamName, id.getValue(), e.getDrivingId());

        } catch (Exception ex) {
            log.error("Redis Stream 발행 실패: drivingId={}", e.getDrivingId(), ex);
            throw new BusinessException(
                    DrivingErrorCode.REDIS_STREAM_PUBLISH_FAILED,
                    "Redis Stream 이벤트 발행 중 오류: " + e.getDrivingId()
            );
        }
    }
}