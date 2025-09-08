package com.smooth.driving_analysis_service.pipeline.service;

import com.smooth.driving_analysis_service.driving.entity.DrivingRecord;
import com.smooth.driving_analysis_service.driving.repository.DrivingRecordRepository;
import com.smooth.driving_analysis_service.pipeline.entity.DrivingAccumulatedStats;
import com.smooth.driving_analysis_service.pipeline.repository.DrivingAccumulatedStatsRepository;
import com.smooth.driving_analysis_service.trigger.dto.DrivingSummaryV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrivingDataPipelineService {

    private final DrivingRecordRepository drivingRecordRepository;
    private final DrivingAccumulatedStatsRepository drivingAccumulatedStatsRepository;

    /**
     * XADD 메시지와 DrivingRecord를 통합하여 누적 통계 저장
     */
    @Transactional
    public void integrateAndStore(DrivingSummaryV1 xaddData) {
        log.info("[PIPELINE] Integrating data for drivingId: {}", xaddData.getDrivingId());

        try {
            // DrivingRecord 조회
            Optional<DrivingRecord> drivingRecordOpt = drivingRecordRepository
                    .findByDrivingId(xaddData.getDrivingId());

            if (drivingRecordOpt.isEmpty()) {
                log.warn("[PIPELINE] DrivingRecord not found for drivingId: {}", xaddData.getDrivingId());
                // DrivingRecord가 없어도 XADD 데이터만으로 저장
                saveDrivingAccumulatedStats(xaddData, null);
                return;
            }

            DrivingRecord drivingRecord = drivingRecordOpt.get();
            
            // 통합 데이터 저장
            saveDrivingAccumulatedStats(xaddData, drivingRecord);
            
            log.info("[PIPELINE] Successfully integrated data for drivingId: {}", xaddData.getDrivingId());

        } catch (Exception e) {
            log.error("[PIPELINE] Failed to integrate data for drivingId: {}", xaddData.getDrivingId(), e);
            throw e;
        }
    }

    private void saveDrivingAccumulatedStats(DrivingSummaryV1 xaddData, DrivingRecord drivingRecord) {
        DrivingAccumulatedStats.DrivingAccumulatedStatsBuilder builder = DrivingAccumulatedStats.builder()
                .userId(Long.valueOf(xaddData.getUserId()))
                .drivingId(xaddData.getDrivingId())
                // XADD 데이터
                .drivingMinutes(xaddData.getDrivingMinutes())
                .totalDistance(xaddData.getTotalDistance())
                .laneChangeCount(xaddData.getLaneChangeCount())
                .hardBrakeCount(xaddData.getHardBrakeCount())
                .rapidAccelCount(xaddData.getRapidAccelCount());

        // DrivingRecord 데이터 (있는 경우)
        if (drivingRecord != null) {
            builder.avgSpeed(drivingRecord.getAvgSpeed())
                    .cruiseRatio(drivingRecord.getCruiseRatio())
                    .startTime(drivingRecord.getStartTime())
                    .endTime(drivingRecord.getEndTime());
        } else {
            // DrivingRecord가 없는 경우 XADD 데이터에서 시간 정보 파싱
            builder.startTime(parseDateTime(xaddData.getStartedAt()))
                    .endTime(parseDateTime(xaddData.getEndedAt()));
        }

        DrivingAccumulatedStats stats = builder.build();
        drivingAccumulatedStatsRepository.save(stats);

        log.debug("[PIPELINE] Saved DrivingAccumulatedStats: drivingId={}, userId={}", 
                stats.getDrivingId(), stats.getUserId());
    }

    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            log.warn("[PIPELINE] Failed to parse integer: {}", value);
            return null;
        }
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            log.warn("[PIPELINE] Failed to parse datetime: {}", value);
            return null;
        }
    }
    
    private LocalDateTime parseDateTime(Long epochMilli) {
        if (epochMilli == null || epochMilli <= 0) {
            return null;
        }
        try {
            return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(epochMilli), 
                ZoneId.of("Asia/Seoul")
            );
        } catch (Exception e) {
            log.warn("[PIPELINE] Failed to parse datetime from epoch: {}", epochMilli);
            return null;
        }
    }
}