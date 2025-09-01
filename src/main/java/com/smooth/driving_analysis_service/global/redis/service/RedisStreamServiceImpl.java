package com.smooth.driving_analysis_service.global.redis.service;

import com.smooth.driving_analysis_service.driving.exception.DrivingErrorCode;
import com.smooth.driving_analysis_service.global.exception.BusinessException;
import com.smooth.driving_analysis_service.global.redis.dto.DrivingEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisStreamServiceImpl implements RedisStreamService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${redis.stream.driving.name}")
    private String drivingStreamName;

    @Override
    public void publishDrivingEvent(DrivingEventDto eventDto) {
        try {
            Map<String, Object> streamData = Map.ofEntries(
                    Map.entry("v", String.valueOf(eventDto.getV())),
                    Map.entry("userId", eventDto.getUserId().toString()),
                    Map.entry("drivingId", eventDto.getDrivingId()),
                    Map.entry("startTime", eventDto.getStartTime().toString()),
                    Map.entry("endTime", eventDto.getEndTime().toString()),
                    Map.entry("status", eventDto.getStatus()),
                    Map.entry("drivingMinutes", String.valueOf(eventDto.getDrivingMinutes())),
                    Map.entry("totalDistance", String.valueOf(eventDto.getTotalDistance())),
                    Map.entry("laneChangeCount", String.valueOf(eventDto.getLaneChangeCount())),
                    Map.entry("hardBrakeCount", String.valueOf(eventDto.getHardBrakeCount())),
                    Map.entry("rapidAccelCount", String.valueOf(eventDto.getRapidAccelCount()))
            );

            redisTemplate.opsForStream().add(drivingStreamName, streamData);
            log.info("주행 이벤트 발행 완료: {}", eventDto.getDrivingId());

        } catch (Exception e) {
            log.error("Redis Stream 발행 실패: drivingId={}", eventDto.getDrivingId(), e);
            throw new BusinessException(DrivingErrorCode.REDIS_STREAM_PUBLISH_FAILED,
                    "Redis Stream 이벤트 발행 중 오류가 발생했습니다: " + eventDto.getDrivingId());
        }
    }

}
