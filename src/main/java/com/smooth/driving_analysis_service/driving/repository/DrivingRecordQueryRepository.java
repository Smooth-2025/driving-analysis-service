package com.smooth.driving_analysis_service.driving.repository;

import com.smooth.driving_analysis_service.driving.entity.DrivingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DrivingRecordQueryRepository extends JpaRepository<DrivingRecord, Long> {

    @Query("SELECT d FROM DrivingRecord d WHERE d.userId = :userId AND d.endTime >= :startDate AND d.endTime < :endDate")
    List<DrivingRecord> findRecordsByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}