package com.smooth.driving_analysis_service.reports.progress.service;

import com.smooth.driving_analysis_service.reports.progress.dto.ProgressResponseDto;
import com.smooth.driving_analysis_service.reports.progress.repository.UserCycleAccumulatorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressService {

    private final UserCycleAccumulatorRepository accRepo;

    @Value("${progress.threshold:15}")
    private int threshold;

    public ProgressResponseDto getProgress(long userId) {
        int totalTrips = accRepo.sumTripsByUser(userId);

        String cycleNo = null;
        int currentCount = totalTrips % threshold;

        var latestOpt = accRepo.findLatestByUserId(userId);
        if (latestOpt.isPresent()) {
            var v = latestOpt.get();
            cycleNo = v.getCycleNo();
            if (v.getCurrentCycleCount() != null) {
                currentCount = v.getCurrentCycleCount();
            }
        } else if (totalTrips > 0) {
            cycleNo = String.valueOf((totalTrips - 1) / threshold + 1);
        }

        int remaining = Math.max(0, threshold - currentCount);

        log.debug("[US7.1] userId={}, totalTrips={}, cycleNo={}, currentCount={}, remaining={}",
                userId, totalTrips, cycleNo, currentCount, remaining);

        return ProgressResponseDto.builder()
                .userId(userId)
                .cycleNo(cycleNo)
                .totalTrips(totalTrips)
                .currentCycleCount(currentCount)
                .threshold(threshold)
                .remainingTrips(remaining)
                .build();
    }
}
