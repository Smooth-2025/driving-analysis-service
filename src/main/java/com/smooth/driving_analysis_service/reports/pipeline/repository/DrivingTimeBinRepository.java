package com.smooth.driving_analysis_service.reports.pipeline.repository;

import com.smooth.driving_analysis_service.reports.pipeline.entity.DrivingTimeBin;
import org.springframework.data.jpa.repository.*;
import org.springframework.transaction.annotation.Transactional;

public interface DrivingTimeBinRepository extends JpaRepository<DrivingTimeBin, Long> {

    @Transactional
    @Modifying
    @Query("delete from DrivingTimeBin b where b.drivingId = :drivingId")
    void deleteByDrivingId(String drivingId);
}
