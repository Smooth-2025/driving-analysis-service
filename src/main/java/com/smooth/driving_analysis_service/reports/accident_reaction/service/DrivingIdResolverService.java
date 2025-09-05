package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import java.time.LocalDateTime;

public interface DrivingIdResolverService {
    /** renderedAt을 포함(1순위) 또는 근접(옵션)하는 drivingId 반환. 없으면 null */
    String resolve(long userId, LocalDateTime renderedAt);
}
