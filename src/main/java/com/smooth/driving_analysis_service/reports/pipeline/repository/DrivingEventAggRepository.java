package com.smooth.driving_analysis_service.reports.pipeline.repository;

import com.smooth.driving_analysis_service.reports.pipeline.entity.DrivingEventAgg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DrivingEventAggRepository extends JpaRepository<DrivingEventAgg, String> {
}