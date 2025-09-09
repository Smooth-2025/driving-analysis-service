package com.smooth.driving_analysis_service.reports.pipeline.repository;

import com.smooth.driving_analysis_service.reports.pipeline.entity.DrivingEventAgg;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DrivingEventAggRepository extends JpaRepository<DrivingEventAgg, String> {
    // PK = drivingId (String)
}
