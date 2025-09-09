// src/main/java/com/smooth/driving_analysis_service/pipeline/RealtimeDrivingService.java
package com.smooth.driving_analysis_service.reports.pipeline;

import com.smooth.driving_analysis_service.driving.entity.DrivingRecord;
import com.smooth.driving_analysis_service.driving.entity.SummaryStatus;
import com.smooth.driving_analysis_service.driving.repository.DrivingRecordRepository;
import com.smooth.driving_analysis_service.reports.pipeline.entity.DrivingEventAgg;
import com.smooth.driving_analysis_service.reports.pipeline.entity.DrivingTimeBin;
import com.smooth.driving_analysis_service.reports.pipeline.entity.DrivingAccumulatedStats;
import com.smooth.driving_analysis_service.reports.pipeline.repository.DrivingEventAggRepository;
import com.smooth.driving_analysis_service.reports.pipeline.repository.DrivingTimeBinRepository;
import com.smooth.driving_analysis_service.reports.pipeline.repository.DrivingAccumulatedStatsRepository;
import com.smooth.driving_analysis_service.reports.pipeline.support.DtoIntrospector;
import com.smooth.driving_analysis_service.reports.trigger.dto.DrivingSummaryV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RealtimeDrivingService {

    private final DrivingRecordRepository drivingRepo;
    private final DrivingEventAggRepository eventAggRepo;
    private final DrivingTimeBinRepository timeBinRepo;
    private final DrivingAccumulatedStatsRepository accumulatedStatsRepo;

    @Transactional
    public void applySummary(DrivingSummaryV1 s) {
        s.validateForProcessing(); // DTO가 제공

        // === DTO에서 실제 필드명으로 꺼내기 ===
        final String drivingId = DtoIntrospector.str(s, "getDrivingId");
        final Long   userId    = Long.valueOf(DtoIntrospector.str(s, "getUserId"));

        final Integer totalDistanceM = DtoIntrospector.integer(s, "getTotalDistance");   // m
        final Integer drivingMinutes = DtoIntrospector.integer(s, "getDrivingMinutes");  // min

        final Integer evLane  = DtoIntrospector.integer(s, "getLaneChangeCount");
        final Integer evHard  = DtoIntrospector.integer(s, "getHardBrakeCount");
        final Integer evRapid = DtoIntrospector.integer(s, "getRapidAccelCount");

        LocalDateTime startedAt = s.getStartedAtAsDateTime();
        LocalDateTime endedAt   = s.getEndedAtAsDateTime();

        // === 파생값 ===
        final Double distanceKm = (totalDistanceM == null ? null : totalDistanceM / 1000.0);
        final Integer durationS = (drivingMinutes == null ? null : drivingMinutes * 60);
        final Double avgSpeed   = (distanceKm != null && durationS != null && durationS > 0)
                ? (distanceKm / (durationS / 3600.0)) : null;

        // === driving_record upsert (세터 명이 달라도 안전하게 패치) ===
        DrivingRecord rec = drivingRepo.findByDrivingId(drivingId).orElse(null);

        if (rec == null) {
            rec = DrivingRecord.createInitialRecord(drivingId, userId);
        }

// 이후 distance/avgSpeed 등 업데이트
        if (startedAt != null) rec = DrivingRecord.builder()
                .id(rec.getId()) // 기존 있으면 유지
                .drivingId(rec.getDrivingId())
                .userId(rec.getUserId())
                .startTime(startedAt)
                .endTime(endedAt)
                .totalDistance(distanceKm)
                .avgSpeed(avgSpeed)
                .laneChangeCount(evLane != null ? evLane : rec.getLaneChangeCount())
                .hardBrakeCount(evHard != null ? evHard : rec.getHardBrakeCount())
                .rapidAccelCount(evRapid != null ? evRapid : rec.getRapidAccelCount())
                .sharpTurnCount(rec.getSharpTurnCount())
                .status(SummaryStatus.COMPLETED)
                .build();

        drivingRepo.save(rec);

        // === driving_accumulated_stats upsert (XADD + DrivingRecord 통합) ===
        DrivingAccumulatedStats accStats = accumulatedStatsRepo.findByDrivingId(drivingId)
                .orElse(DrivingAccumulatedStats.builder()
                        .userId(userId)
                        .drivingId(drivingId)
                        .build());
        
        // XADD 필드들
        accStats.setDrivingMinutes(drivingMinutes);
        accStats.setTotalDistance(totalDistanceM);
        accStats.setLaneChangeCount(evLane);
        accStats.setHardBrakeCount(evHard);
        accStats.setRapidAccelCount(evRapid);
        
        // DrivingRecord 필드들
        accStats.setAvgSpeed(avgSpeed);
        accStats.setCruiseRatio(rec.getCruiseRatio()); // DrivingRecord에서 가져옴
        accStats.setStartTime(startedAt);
        accStats.setEndTime(endedAt);
        
        accumulatedStatsRepo.save(accStats);
        log.debug("DrivingAccumulatedStats saved for drivingId={}", drivingId);

        // === driving_event_agg upsert ===
        DrivingEventAgg agg = eventAggRepo.findById(drivingId)
                .orElse(DrivingEventAgg.builder()
                        .drivingId(drivingId).userId(userId)
                        .laneChangeCount(0).hardBrakeCount(0).rapidAccelCount(0).sharpTurnCount(0)
                        .build());
        if (evLane  != null) agg.setLaneChangeCount(evLane);
        if (evHard  != null) agg.setHardBrakeCount(evHard);
        if (evRapid != null) agg.setRapidAccelCount(evRapid);
        eventAggRepo.save(agg);

        // === 시간대 bin ===
        LocalDateTime binStart = startedAt;
        LocalDateTime binEnd   = endedAt;
        if (binStart != null && (binEnd != null || durationS != null)) {
            if (binEnd == null) binEnd = binStart.plusSeconds(durationS);
            Map<Integer, Integer> bins = splitMinutesByHour(binStart, binEnd);
            timeBinRepo.deleteByDrivingId(drivingId);
            for (var e : bins.entrySet()) {
                timeBinRepo.save(DrivingTimeBin.builder()
                        .drivingId(drivingId).userId(userId)
                        .hourOfDay(e.getKey()).minutes(e.getValue())
                        .build());
            }
        }
    }

    private static Map<Integer, Integer> splitMinutesByHour(LocalDateTime start, LocalDateTime end) {
        Map<Integer, Integer> bins = new LinkedHashMap<>();
        if (start == null || end == null || !end.isAfter(start)) return bins;
        var cur = start;
        while (!cur.isAfter(end)) {
            var endOfHour = cur.withMinute(59).withSecond(59).withNano(0);
            var sliceEnd  = end.isBefore(endOfHour) ? end : endOfHour;
            long minutes  = java.time.Duration.between(cur, sliceEnd).toMinutes();
            if (minutes < 0) minutes = 0;
            bins.merge(cur.getHour(), (int) minutes, Integer::sum);
            cur = sliceEnd.plusSeconds(1);
        }
        return bins;
    }
}
