package com.smooth.driving_analysis_service.snapshot.repository;

import com.smooth.driving_analysis_service.snapshot.entity.CycleAccumulatorEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CycleAccumulatorRepository extends JpaRepository<CycleAccumulatorEntity, Long> {

    Optional<CycleAccumulatorEntity> findByUserId(Long userId);

    /** T7.3.2에서 증가/리셋 시 동시성 제어용 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from CycleAccumulatorEntity a where a.userId = :userId")
    Optional<CycleAccumulatorEntity> findByUserIdForUpdate(@Param("userId") Long userId);
}
