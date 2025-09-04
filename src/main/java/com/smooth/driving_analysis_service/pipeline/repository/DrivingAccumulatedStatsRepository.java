package com.smooth.driving_analysis_service.pipeline.repository;

import com.smooth.driving_analysis_service.pipeline.entity.DrivingAccumulatedStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DrivingAccumulatedStatsRepository extends JpaRepository<DrivingAccumulatedStats, Long> {
    
    Optional<DrivingAccumulatedStats> findByDrivingId(String drivingId);
    
    boolean existsByDrivingId(String drivingId);
}