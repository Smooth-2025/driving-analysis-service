package com.smooth.driving_analysis_service.reports.batch.scheduler;

import com.smooth.driving_analysis_service.reports.batch.service.CycleReportBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 사이클 기반 리포트 스케줄러
 * - 매일 새벽 2시(KST) 실행
 * - 사용자별 numberOfDriving 기준으로 INTERIM/FINAL 분기 처리
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class CycleReportScheduler {
    
    private final CycleReportBatchService cycleReportBatchService;
    
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    
    /**
     * 운영: 매일 새벽 2시 실행
     */
    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Seoul")
    public void processCycleReports() {
        LocalDateTime now = LocalDateTime.now(KST);
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        log.info("=== Cycle Report Batch Started at {} ===", now);
        
        try {
            cycleReportBatchService.processAllUsers(dateStr);
            log.info("Cycle reports batch processing completed successfully");
            
        } catch (Exception e) {
            log.error("Failed to process cycle reports batch", e);
        }
        
        log.info("=== Cycle Report Batch Finished ===");
    }
    
    /**
     * 개발: 10분 간격 실행 (옵션)
     */
    @Scheduled(fixedRate = 600000) // 10분 = 600,000ms
    @Profile("dev")
    public void processCycleReportsForDev() {
        LocalDateTime now = LocalDateTime.now(KST);
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        log.info("=== [DEV] Cycle Report Batch Started at {} ===", now);
        
        try {
            cycleReportBatchService.processAllUsers(dateStr);
            log.info("[DEV] Cycle reports batch processing completed successfully");
            
        } catch (Exception e) {
            log.error("[DEV] Failed to process cycle reports batch", e);
        }
        
        log.info("=== [DEV] Cycle Report Batch Finished ===");
    }
}