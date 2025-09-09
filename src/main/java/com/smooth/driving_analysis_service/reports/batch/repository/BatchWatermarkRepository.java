package com.smooth.driving_analysis_service.reports.batch.repository;

import com.smooth.driving_analysis_service.reports.batch.entity.BatchWatermark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BatchWatermarkRepository extends JpaRepository<BatchWatermark, Long> {

    @Query("select w from BatchWatermark w where w.id = 1")
    BatchWatermark getSingleton();
}
