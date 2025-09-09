package com.smooth.driving_analysis_service.reports.batch.athena;

import java.time.Instant;

public interface AthenaStagePort {
    void refreshForUserUntil(Long userId, Instant cutoff);
}
