package com.smooth.driving_analysis_service.global.redis.service;

import com.smooth.driving_analysis_service.global.redis.dto.DrivingEventDto;

public interface RedisStreamService {

    void publishDrivingEvent(DrivingEventDto eventDto);

}
