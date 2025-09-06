package com.smooth.driving_analysis_service.driving.repository;

import com.smooth.driving_analysis_service.driving.entity.DrivingRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DrivingRecordRepository extends JpaRepository<DrivingRecord, Long> {

        @Query("SELECT d FROM DrivingRecord d WHERE d.userId = :userId AND d.createdAt < :cursor ORDER BY d.createdAt DESC")
        List<DrivingRecord> findByUserIdAndCreatedAtBefore(
                        @Param("userId") Long userId,
                        @Param("cursor") LocalDateTime cursor,
                        Pageable pageable);

        @Query("SELECT d FROM DrivingRecord d WHERE d.userId = :userId ORDER BY d.createdAt DESC")
        List<DrivingRecord> findByUserIdOrderByCreatedAtDesc(
                        @Param("userId") Long userId,
                        Pageable pageable);

        long countByUserId(Long userId);

        Page<DrivingRecord> findByUserIdAndEndTimeBeforeOrderByEndTimeDesc(Long userId, LocalDateTime before,
                        Pageable pageable);

        Page<DrivingRecord> findByUserIdOrderByEndTimeDesc(Long userId, Pageable pageable);

        Optional<DrivingRecord> findByDrivingId(String drivingId);
}
