package com.smooth.driving_analysis_service.reports.progress.service;

import com.smooth.driving_analysis_service.reports.progress.dto.ReadinessResponseDto;
import com.smooth.driving_analysis_service.reports.progress.repository.UserCycleAccumulatorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReadinessService {

    private final UserCycleAccumulatorRepository accRepo;

    @Value("${progress.threshold:15}")
    private int threshold;

    public ReadinessResponseDto getReadiness(long userId) {
        var latestOpt = accRepo.findLatestByUserId(userId);
        int totalTrips = accRepo.sumTripsByUser(userId);

        String cycleNo   = latestOpt.map(UserCycleAccumulatorRepository.LatestView::getCycleNo).orElse(null);
        int currentCount = latestOpt.map(UserCycleAccumulatorRepository.LatestView::getCurrentCycleCount)
                .orElse(totalTrips % threshold);

        boolean ready = currentCount >= threshold;
        int missing   = ready ? 0 : Math.max(0, threshold - currentCount);

        log.debug("[US7.1] readiness userId={}, ready={}, missing={}, currentCount={}, totalTrips={}",
                userId, ready, missing, currentCount, totalTrips);

        return ReadinessResponseDto.builder()
                .userId(userId)
                .cycleNo(cycleNo)
                .ready(ready)
                .missingTrips(missing)
                .currentCycleCount(currentCount)
                .totalTrips(totalTrips)
                .threshold(threshold)
                .build();
    }
}
