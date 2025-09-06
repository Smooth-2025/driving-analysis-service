package com.smooth.driving_analysis_service.pipeline.repository;

import com.smooth.driving_analysis_service.pipeline.entity.DrivingEventAgg;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DrivingEventAggRepository extends JpaRepository<DrivingEventAgg, String> {
    // PK = drivingId (String)
}
