package com.smooth.driving_analysis_service.snapshot.service;

import com.smooth.driving_analysis_service.snapshot.dto.SnapshotThresholdViewDto;
import com.smooth.driving_analysis_service.snapshot.entity.CycleAccumulatorEntity;
import com.smooth.driving_analysis_service.snapshot.repository.CycleAccumulatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SnapshotThresholdService {

    public static final int THRESHOLD = 15;

    private final CycleAccumulatorRepository accumulatorRepository;

    /** 읽기 전용: 현재 사이클 진행도를 기반으로 15회 도달/임박 여부 판정 */
    @Transactional(readOnly = true)
    public SnapshotThresholdViewDto view(Long userId) {
        CycleAccumulatorEntity acc = accumulatorRepository.findByUserId(userId)
                .orElse(CycleAccumulatorEntity.newEmpty(userId));
        return SnapshotThresholdViewDto.of(THRESHOLD, acc.getTripCount(), acc.getCycleIndex());
    }

    /** 다음 트립 저장 시 15회 도달 여부 */
    @Transactional(readOnly = true)
    public boolean willReachAfterNext(Long userId) {
        return view(userId).willReachAfterNext();
    }

    /** 지금 당장 15회 이상인지(실무상 바로 발행되므로 true일 시간창은 짧음) */
    @Transactional(readOnly = true)
    public boolean eligibleNow(Long userId) {
        return view(userId).eligibleNow();
    }
}
