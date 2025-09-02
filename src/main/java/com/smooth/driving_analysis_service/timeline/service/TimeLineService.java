package com.smooth.driving_analysis_service.timeline.service;

import com.smooth.driving_analysis_service.timeline.dto.TimeLineResponseDto;

public interface TimeLineService {
    TimeLineResponseDto getAllTimeLine(Long userId, String cursor, int limit);
    TimeLineResponseDto getReportTimeLine(Long userId, String cursor, int limit);
    TimeLineResponseDto getDrivingTimeLine(Long userId, String cursor, int limit);
}
