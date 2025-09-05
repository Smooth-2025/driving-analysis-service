package com.smooth.driving_analysis_service.reports.accident_reaction.repository;

import com.smooth.driving_analysis_service.reports.accident_reaction.entity.AccidentReactionMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AccidentReactionMetricRepository extends JpaRepository<AccidentReactionMetric, Long> {
    
    List<AccidentReactionMetric> findByDrivingIdIn(List<String> drivingIds);
    
    List<AccidentReactionMetric> findByUserId(Long userId);
    
    List<AccidentReactionMetric> findByDrivingId(String drivingId);
    
    @Query("""
        SELECT 
            COUNT(*) as alertsReceived,
            AVG(CASE WHEN m.responseTimeMs IS NOT NULL THEN m.responseTimeMs END) as avgResponseMs,
            AVG(CASE WHEN m.reactionType = 'BRAKE' OR m.reactionType = 'STOP' THEN 1.0 ELSE 0.0 END) as brakeOrStopRatio,
            AVG(CASE WHEN m.reactionType = 'EVASIVE' THEN 1.0 ELSE 0.0 END) as evasiveRatio
        FROM AccidentReactionMetric m 
        WHERE m.userId = :userId 
        AND m.createdAt BETWEEN :from AND :to
        """)
    Object[] summary(@Param("userId") long userId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}