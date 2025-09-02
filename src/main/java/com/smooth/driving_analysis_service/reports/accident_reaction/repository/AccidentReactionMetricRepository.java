package com.smooth.driving_analysis_service.reports.accident_reaction.repository;

import com.smooth.driving_analysis_service.reports.accident_reaction.entity.AccidentReactionMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface AccidentReactionMetricRepository extends JpaRepository<AccidentReactionMetric, Long> {

    @Query("""
      select count(m), avg(m.reactionMs),
             avg(case when m.decelOrStop=true then 1.0 else 0.0 end),
             avg(case when m.evasiveManeuver=true then 1.0 else 0.0 end)
        from AccidentReactionMetric m
       where m.userId = :userId
         and m.createdAt between :from and :to
    """)
    Object[] summary(long userId, LocalDateTime from, LocalDateTime to);
}
