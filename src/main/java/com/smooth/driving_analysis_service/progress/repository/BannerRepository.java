package com.smooth.driving_analysis_service.progress.repository;

import com.smooth.driving_analysis_service.progress.entity.BannerDomain;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface BannerRepository extends JpaRepository<BannerDomain, String> {

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
