package com.smooth.driving_analysis_service.reports.milestone.service;

import com.smooth.driving_analysis_service.driving.repository.DrivingRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * T7.2.1: 15의 배수 도달 판단 로직 + 커밋 트리거 연동
 *  - 주행 커밋 시 호출
 *  - READY 주행 누적이 15의 배수면 MilestoneReachedEvent 발행
 *  - 실제 리포트 생성/아이템 적재는 T7.2.2 / T7.2.3에서 이 이벤트를 구독하여 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MilestoneTriggerService {

    private static final int THRESHOLD = 15;

    private final DrivingRecordRepository drivingRecordRepository;
    private final ApplicationEventPublisher publisher;

    @Transactional(readOnly = true)
    public boolean onTripCommitted(Long userId) {
        long totalReady = drivingRecordRepository.countByUserId(userId);
        if (totalReady <= 0 || totalReady % THRESHOLD != 0) {
            log.debug("[MilestoneTrigger] skip: userId={}, totalReady={}", userId, totalReady);
            return false;
        }
        int milestone = Math.toIntExact(totalReady);
        log.info("[MilestoneTrigger] reached: userId={}, milestone={}", userId, milestone);

        // 👉 여기서 “이벤트만” 발행 (생성/적재는 다음 태스크에서)
        publisher.publishEvent(new com.smooth.driving_analysis_service.reports.milestone.event.MilestoneReachedEvent(userId, String.valueOf(milestone)));
        return true;
    }
}
