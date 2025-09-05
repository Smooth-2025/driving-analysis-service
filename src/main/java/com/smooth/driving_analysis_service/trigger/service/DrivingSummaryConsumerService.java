package com.smooth.driving_analysis_service.trigger.service;

import com.smooth.driving_analysis_service.reports.milestone.service.MilestoneService;
import com.smooth.driving_analysis_service.global.redis.RedisKeys;
import com.smooth.driving_analysis_service.trigger.dto.DrivingSummaryV1;
import com.smooth.driving_analysis_service.pipeline.RealtimeDrivingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrivingSummaryConsumerService {

    private final RedisTemplate<String, String> redis;
    private final RealtimeDrivingService pipelineService;
    private final MilestoneService milestoneService;

    @Transactional
    public void processDrivingSummary(String messageId, DrivingSummaryV1 s) {
        if (!s.isCompleted()) return;

        // 1) 멱등성(트립단위)
        String idemKey = RedisKeys.processedTrip(s.getDrivingId());
        Boolean first = redis.opsForValue().setIfAbsent(idemKey, "1", Duration.ofDays(7));
        if (Boolean.FALSE.equals(first)) {
            log.debug("skip duplicate trip {}", s.getDrivingId());
            return;
        }

        // 2) Pipeline 통합 통계 저장 (XADD + DrivingRecord → driving_accumulated_stats)
        pipelineService.applySummary(s);
        log.debug("Pipeline processing completed for drivingId={}", s.getDrivingId());

        // 3) 마일스톤 처리 (MilestoneService에 위임)
        Long userId = Long.valueOf(s.getUserId());
        milestoneService.processDrivingCompleted(userId, s.getDrivingId());
        
        log.info("Driving summary processed: userId={}, drivingId={}", userId, s.getDrivingId());
    }



    public void handle(DrivingSummaryV1 dto) {
        processDrivingSummary(null, dto);
    }
}