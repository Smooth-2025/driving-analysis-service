package com.smooth.driving_analysis_service.reports.accident_reaction.repository;

import com.smooth.driving_analysis_service.reports.accident_reaction.entity.AccidentReactionMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccidentReactionMetricRepository extends JpaRepository<AccidentReactionMetric, Long> {
    List<AccidentReactionMetric> findByDrivingIdIn(List<String> drivingIds);
    List<AccidentReactionMetric> findByDrivingId(String drivingId);
}