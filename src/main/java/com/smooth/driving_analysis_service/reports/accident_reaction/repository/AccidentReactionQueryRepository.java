package com.smooth.driving_analysis_service.reports.accident_reaction.repository;

import com.smooth.driving_analysis_service.reports.accident_reaction.entity.AccidentReactionMetric;

import java.util.List;

public interface AccidentReactionQueryRepository {
    /** 예: 최근 created_at 순으로 n건 */
    List<AccidentReactionMetric> findLatestByDrivingIds(List<String> drivingIds, int limitPerDriving);
}
