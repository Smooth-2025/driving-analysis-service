package com.smooth.driving_analysis_service.batch.scheduler;

import com.smooth.driving_analysis_service.batch.service.NightlyBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class NightlyBatchScheduler {

    private final NightlyBatchService nightly;

    @Scheduled(cron = "0 5 2 * * *", zone = "Asia/Seoul")
    public void runNightlyKst() {
        LocalDate target = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1);
        int processed = nightly.runOperational(target);
        log.info("[Scheduler] nightly completed - date={}, processed={}", target, processed);
    }
}
