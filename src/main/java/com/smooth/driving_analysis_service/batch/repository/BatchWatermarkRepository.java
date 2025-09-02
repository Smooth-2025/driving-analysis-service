package com.smooth.driving_analysis_service.batch.repository;

import com.smooth.driving_analysis_service.batch.entity.BatchWatermark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchWatermarkRepository extends JpaRepository<BatchWatermark, Integer> {
    @Query("select w from BatchWatermark w where w.id = 1")
    BatchWatermark getSingleton();
}
