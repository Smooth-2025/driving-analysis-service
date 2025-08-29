package com.smooth.driving_analysis_service.timeline.service;

import com.smooth.driving_analysis_service.timeline.dto.TimeLineResponseDto;

public interface TimeLineService {

    TimeLineResponseDto getDrivingTimeLine(Long userId, String cursor, int limit);
}
