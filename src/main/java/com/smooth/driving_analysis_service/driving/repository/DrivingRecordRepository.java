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
public interface DrivingRecordRepository extends JpaRepository<DrivingRecord,Long> {

    @Query("SELECT e FROM DrivingRecord e WHERE DATE(e.endTime) = CURRENT_DATE AND e.userId = :userId")
    List<DrivingRecord> findByUserIdAndEndTimeToday(@Param("userId") Long userId);

    @Query("SELECT d FROM DrivingRecord d WHERE d.userId = :userId AND d.endTime >= :startDate AND d.endTime < :endDate")
    List<DrivingRecord> findByUserIdAndEndTimeBetweenAndStatus(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT d FROM DrivingRecord d WHERE d.userId = :userId AND d.createdAt < :cursor ORDER BY d.createdAt DESC")
    List<DrivingRecord> findByUserIdAndCreatedAtBefore(
            @Param("userId") Long userId,
            @Param("cursor") LocalDateTime cursor,
            Pageable pageable
    );

    @Query("SELECT d FROM DrivingRecord d WHERE d.userId = :userId ORDER BY d.createdAt DESC")
    List<DrivingRecord> findByUserIdOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("SELECT dr FROM DrivingRecord dr WHERE dr.id BETWEEN :fromRecordId AND :toRecordId AND dr.userId = :userId ORDER BY dr.id")
    List<DrivingRecord> findRecordsByIdRangeAndUserId(@Param("fromRecordId") Long fromRecordId,
                                                      @Param("toRecordId") Long toRecordId,
                                                      @Param("userId") Long userId);

    long countByUserId(Long userId);
    Page<DrivingRecord> findByUserIdAndEndTimeBeforeOrderByEndTimeDesc(Long userId, LocalDateTime before, Pageable pageable);
    Page<DrivingRecord> findByUserIdOrderByEndTimeDesc(Long userId, Pageable pageable);
    Optional<DrivingRecord> findByDrivingId(String drivingId);
}
