package com.smooth.driving_analysis_service.progress.service;

import com.smooth.driving_analysis_service.progress.domain.DrivingRecordDomain;
import com.smooth.driving_analysis_service.progress.dto.ReportProgressDto;
import com.smooth.driving_analysis_service.progress.repository.DrivingRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportProgressService {

    private static final int SNAPSHOT_THRESHOLD = 15;

    private final DrivingRecordRepository drivingTripRepository;

    @Transactional(readOnly = true)
    public ReportProgressDto getProgress(String userId) {
        long total = drivingTripRepository.countByUserId(userId);
        long remaining = Math.max(0, SNAPSHOT_THRESHOLD - total);
        double progress = Math.min(1.0d, (double) total / SNAPSHOT_THRESHOLD);

        return ReportProgressDto.builder()
                .totalTrips(total)
                .threshold(SNAPSHOT_THRESHOLD)
                .remainingToThreshold(remaining)
                .progressRate(progress)
                .build();
    }
}
