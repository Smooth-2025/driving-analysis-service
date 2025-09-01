package com.smooth.driving_analysis_service.reports.milestone.batch.scheduler;

import com.smooth.driving_analysis_service.reports.milestone.batch.service.NightlyBatchService;
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

    private final NightlyBatchService nightlyBatchService;

    // KST 09:30에 실행
    @Scheduled(cron = "0 30 9 * * *", zone = "Asia/Seoul")
    public void run0930Kst() {
        ZoneId KST = ZoneId.of("Asia/Seoul");
        int processed = nightlyBatchService.runNightly(LocalDate.now(KST));
        log.info("[Scheduler] nightly finished, processed={}", processed);
    }
}
