package com.smooth.driving_analysis_service.reports.milestone.batch.athena;

import java.time.Instant;

public interface AthenaStageService {
    void refreshForUserUntil( Long userId, Instant cutoff );
}