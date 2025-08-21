package com.smooth.driving_analysis_service.progress.service;

import com.smooth.driving_analysis_service.progress.dto.ProgressDto;
import com.smooth.driving_analysis_service.progress.repository.VUserProgress15Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final VUserProgress15Repository repo;

    public ProgressDto getProgress(String userId) {
        return repo.findById(userId)
                .map(e -> ProgressDto.builder()
                        .userId(e.getUserId())
                        .totalTrips(nz(e.getTotalTrips()))
                        .threshold(nz(e.getThreshold()))
                        .remainingTrips(nz(e.getRemainingTrips()))
                        .currentCycleCount(nz(e.getCurrentCycleCount()))
                        .build())
                .orElseGet(() -> ProgressDto.builder()
                        .userId(userId)
                        .totalTrips(0)
                        .threshold(15)
                        .remainingTrips(15)
                        .currentCycleCount(0)
                        .build());
    }

    private int nz(Integer v) { return v == null ? 0 : v; }
}