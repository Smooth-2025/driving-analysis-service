package com.smooth.driving_analysis_service.driving.repository;

import com.smooth.driving_analysis_service.driving.entity.DrivingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrivingRecordRepository extends JpaRepository<DrivingRecord,Long> {

    @Query("SELECT e FROM DrivingRecord e WHERE DATE(e.endTime) = CURRENT_DATE AND e.userId = :userId")
    List<DrivingRecord> findByUserIdAndEndTimeToday(@Param("userId") Long userId);
}
