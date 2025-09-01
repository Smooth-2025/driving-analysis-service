package com.smooth.driving_analysis_service.reports.milestone.batch.athena;

import com.smooth.driving_analysis_service.driving.service.AthenaQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AthenaStageServiceImpl implements AthenaStageService {
    private final AthenaQueryService athena;

    @Override
    public void refreshForUserUntil(Long userId, Instant cutoff) {
        // 1) 파티션/뷰 리프레시
        // 2) 머티리얼라이즈 or 결과 테이블 upsert
        // 3) 필요 시 쿼리 완료 폴링
        // athena.runQuery("... :userId, :cutoff ...");
    }
}