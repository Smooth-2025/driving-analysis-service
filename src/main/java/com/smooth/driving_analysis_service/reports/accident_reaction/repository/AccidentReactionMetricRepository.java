package com.smooth.driving_analysis_service.reports.accident_reaction.repository;

import com.smooth.driving_analysis_service.reports.accident_reaction.entity.AccidentReactionMetric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccidentReactionMetricRepository extends JpaRepository<AccidentReactionMetric, Long> {
    List<AccidentReactionMetric> findByDrivingIdIn(List<String> drivingIds);
}