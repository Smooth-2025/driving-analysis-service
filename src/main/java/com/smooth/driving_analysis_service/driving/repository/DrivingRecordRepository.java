package com.smooth.driving_analysis_service.driving.repository;

import com.smooth.driving_analysis_service.driving.entity.DrivingRecord;
import com.smooth.driving_analysis_service.driving.entity.SummaryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrivingRecordRepository extends JpaRepository<DrivingRecord,Long> {
    long countByUserId(Long userId);
    List<DrivingRecord> findTop15ByUserIdAndStatusOrderByEndTimeDesc(Long userId, SummaryStatus status);
}
