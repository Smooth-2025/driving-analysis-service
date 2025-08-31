package com.smooth.driving_analysis_service.reports.progress.repository;

import com.smooth.driving_analysis_service.reports.progress.entity.UserCycleAccumulator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserCycleAccumulatorRepository extends JpaRepository<UserCycleAccumulator, Long> {

    // 최신 1건(해당 유저의 가장 최근 사이클 상태) 조회용 Projection
    interface LatestView {
        String getCycleNo();
        Integer getCurrentCycleCount();
    }

    @Query(value = """
        SELECT u.cycle_no AS cycleNo,
               u.current_cycle_count AS currentCycleCount
        FROM user_cycle_accumulator u
        WHERE u.user_id = :userId
        ORDER BY u.updated_at DESC, u.id DESC
        LIMIT 1
        """, nativeQuery = true)
    Optional<LatestView> findLatestByUserId(@Param("userId") long userId);

    // ⬇︎ 여기 추가
    @Query(value = """
        SELECT COALESCE(SUM(u.total_trips), 0)
        FROM user_cycle_accumulator u
        WHERE u.user_id = :userId
        """, nativeQuery = true)
    int sumTripsByUser(@Param("userId") long userId);
}
