package com.smooth.driving_analysis_service.progress.service;

import com.smooth.driving_analysis_service.progress.entity.VUserProgress15Entity;
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
                .map(this::toDto)
                .orElse(ProgressDto.builder()
                        .userId(userId)
                        .totalTrips(0)
                        .threshold(15)
                        .remainingTrips(15)
                        .currentCycleCount(0)
                        .reportsGenerated(0)   // ← 추가
                        .build());
    }

    private ProgressDto toDto(VUserProgress15Entity e) {
        int total = nz(e.getTotalTrips());
        int th    = nz(e.getThreshold());
        return ProgressDto.builder()
                .userId(e.getUserId())
                .totalTrips(total)
                .threshold(th)
                .remainingTrips(nz(e.getRemainingTrips()))
                .currentCycleCount(nz(e.getCurrentCycleCount()))
                .reportsGenerated(total / th)   // ← 여기서 계산
                .build();
    }

    private int nz(Integer v) { return v == null ? 0 : v; }
}