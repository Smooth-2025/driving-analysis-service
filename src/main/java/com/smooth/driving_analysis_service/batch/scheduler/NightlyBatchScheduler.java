package com.smooth.driving_analysis_service.batch.scheduler;

import com.smooth.driving_analysis_service.batch.lock.SimpleDistributedLock;
import com.smooth.driving_analysis_service.batch.service.BatchWatermarkService;
import com.smooth.driving_analysis_service.batch.service.NightlyBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class NightlyBatchScheduler {

    private final NightlyBatchService nightlyBatchService;
    private final BatchWatermarkService watermarkService;
    private final SimpleDistributedLock simpleLock;

    @Value("${batch.enabled:true}")
    private boolean enabled;

    @Value("${scheduling.zone:Asia/Seoul}")
    private String zone;

    /** dev/prod 동일 코드, cron은 프로필에서 선택 주입 */
    @Scheduled(cron = "${scheduling.cron.current}", zone = "${scheduling.zone:Asia/Seoul}")
    public void run() {
        if (!enabled) {
            log.info("[BATCH] disabled -> skip");
            return;
        }
        String lockKey = "lock:nightly-batch";
        if (!simpleLock.acquire(lockKey, 9 * 60)) { // 10분 주기면 TTL 9분
            log.warn("[BATCH] lock busy -> skip");
            return;
        }
        try {
            ZoneId kst = ZoneId.of(zone);
            LocalDate asOf = LocalDate.now(kst);

            if (watermarkService.alreadyProcessed(asOf)) {
                log.info("[BATCH] already processed for {} -> idempotent path", asOf);
            }

            nightlyBatchService.runOnce(asOf);

        } catch (Exception e) {
            log.error("[BATCH] error", e);
        } finally {
            simpleLock.release(lockKey);
        }
    }
}
