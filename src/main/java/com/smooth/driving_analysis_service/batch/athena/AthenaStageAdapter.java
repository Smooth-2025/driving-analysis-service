package com.smooth.driving_analysis_service.batch.athena;

import com.smooth.driving_analysis_service.driving.service.AthenaQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class AthenaStageAdapter implements AthenaStagePort {
    private final AthenaQueryService athena;

    @Override
    public void refreshForUserUntil(Long userId, Instant cutoff) {
        // 여기서 AthenaQueryService 호출/조합
        // ex) athena.refreshUserPartitions(userId, cutoff);
        // ex) athena.materializeUserDaily(userId, LocalDate.ofInstant(cutoff, ZoneId.of("Asia/Seoul")));
    }
}
