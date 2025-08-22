package com.smooth.driving_analysis_service.progress.repository;

import com.smooth.driving_analysis_service.progress.entity.BannerEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface BannerRepository extends JpaRepository<BannerEntity, String> {

    @Modifying
    @Query(value = """
        INSERT INTO banner_flag (user_id, acked_reports)
        VALUES (:userId, :reportsGenerated)
        ON DUPLICATE KEY UPDATE
          acked_reports = GREATEST(acked_reports, VALUES(acked_reports)),
          updated_at = CURRENT_TIMESTAMP
        """, nativeQuery = true)
    int upsertAck(@Param("userId") String userId,
                  @Param("reportsGenerated") int reportsGenerated);
}
