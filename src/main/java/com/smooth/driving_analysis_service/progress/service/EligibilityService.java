package com.smooth.driving_analysis_service.progress.service;

import com.smooth.driving_analysis_service.progress.dto.EligibilityDto;
import com.smooth.driving_analysis_service.progress.dto.ProgressDto;
import com.smooth.driving_analysis_service.progress.domain.VUserProgress15;
import com.smooth.driving_analysis_service.progress.repository.VUserProgress15Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EligibilityService {

    private final VUserProgress15Repository repo;

    /** T7.1.2 - 15회 달성 여부 조회 */
    public EligibilityDto getEligibility(String userId) {
        return repo.findById(userId)
                .map(e -> {
                    int total = nz(e.getTotalTrips());
                    int th    = nz(e.getThreshold());          // 15
                    int cur   = nz(e.getCurrentCycleCount());  // 0~15(배수면 15)
                    boolean eligible = total > 0 && (total % th == 0);
                    int missing = eligible ? 0 : (total == 0 ? th : th - cur);

                    return EligibilityDto.builder()
                            .userId(e.getUserId())
                            .eligible(eligible)
                            .missingTrips(missing)
                            .currentCycleCount(cur)
                            .totalTrips(total)
                            .threshold(th)
                            .build();
                })
                .orElse(EligibilityDto.builder()
                        .userId(userId)
                        .eligible(false)
                        .missingTrips(15)
                        .currentCycleCount(0)
                        .totalTrips(0)
                        .threshold(15)
                        .build());
    }

    private ProgressDto toReportProgressDto(VUserProgress15 e) {
        return ProgressDto.builder()
                .userId(e.getUserId())
                .totalTrips(nz(e.getTotalTrips()))
                .threshold(nz(e.getThreshold()))
                .remainingTrips(nz(e.getRemainingTrips()))
                .currentCycleCount(nz(e.getCurrentCycleCount()))
                .build();
    }

    private int nz(Integer v) { return v == null ? 0 : v; }
}
