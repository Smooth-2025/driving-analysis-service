package com.smooth.driving_analysis_service.reports.pipeline.repository;

import com.smooth.driving_analysis_service.reports.pipeline.entity.DrivingTimeBin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DrivingTimeBinRepository extends JpaRepository<DrivingTimeBin, Long> {
    
    void deleteByDrivingId(String drivingId);
}