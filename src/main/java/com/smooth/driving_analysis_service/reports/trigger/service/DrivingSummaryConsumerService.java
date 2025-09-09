package com.smooth.driving_analysis_service.reports.trigger.service;

import com.smooth.driving_analysis_service.reports.pipeline.service.DrivingIntegrationService;
import com.smooth.driving_analysis_service.reports.milestone.service.MilestoneService;
import com.smooth.driving_analysis_service.reports.trigger.dto.DrivingSummaryV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Profile;

import java.time.Duration;

/**
 * 주행 완료 이벤트를 실시간으로 처리하는 서비스
 * 1. 멱등성 체크 (Redis TTL 7일)
 * 2. 통합 통계 저장 (pipeline 호출)
 * 3. 마일스톤 관리 (milestone 호출)
 * 4. 배치 트리거 발행 (마일스톤에서 처리)
 */
@Slf4j
@Service
@Profile("!test")
@RequiredArgsConstructor
public class DrivingSummaryConsumerService {

    private final RedisTemplate<String, String> redisTemplate;
    private final DrivingIntegrationService drivingIntegrationService;
    private final MilestoneService milestoneService;

    @Transactional
    public void processDrivingSummary(String messageId, DrivingSummaryV1 summary) {
        log.info("Processing driving summary: messageId={}, userId={}, drivingId={}", 
                messageId, summary.getUserId(), summary.getDrivingId());
        
        // 유효성 검증
        summary.validateForProcessing();
        
        // 1. 멱등성 체크 (트립 단위, TTL 7일)
        String idempotencyKey = com.smooth.driving_analysis_service.global.redis.RedisKeys.processedTrip(summary.getDrivingId());
        Boolean isFirstTime = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "1", Duration.ofDays(7));
        
        if (Boolean.FALSE.equals(isFirstTime)) {
            log.debug("Skipping duplicate trip: drivingId={}", summary.getDrivingId());
            return;
        }
        
        try {
            // 2. 통합 통계 저장 (pipeline 호출: XADD + DrivingRecord → driving_accumulated_stats)
            drivingIntegrationService.integrateAndSave(summary);
            
            // 3. 마일스톤 관리 (milestone_item, milestone_report 업데이트 + 배치 트리거 발행)
            milestoneService.processDrivingCompleted(Long.valueOf(summary.getUserId()), summary.getDrivingId());
            
            log.info("Driving summary processed successfully: drivingId={}", summary.getDrivingId());
            
        } catch (Exception e) {
            log.error("Failed to process driving summary: drivingId={}", summary.getDrivingId(), e);
            // 멱등성 키 삭제 (재시도 가능하도록)
            redisTemplate.delete(idempotencyKey);
            throw e;
        }
    }

    /**
     * 메시지 ID 없이 처리하는 메서드 (기존 호환성)
     */
    public void handle(DrivingSummaryV1 summary) {
        processDrivingSummary(null, summary);
    }
}