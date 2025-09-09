package com.smooth.driving_analysis_service.reports.pipeline.service;

import com.smooth.driving_analysis_service.driving.entity.DrivingRecord;
import com.smooth.driving_analysis_service.driving.entity.SummaryStatus;
import com.smooth.driving_analysis_service.driving.repository.DrivingRecordRepository;
import com.smooth.driving_analysis_service.reports.pipeline.entity.DrivingAccumulatedStats;
import com.smooth.driving_analysis_service.reports.pipeline.repository.DrivingAccumulatedStatsRepository;
import com.smooth.driving_analysis_service.reports.trigger.dto.DrivingSummaryV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrivingIntegrationServiceImpl implements DrivingIntegrationService {

    private final DrivingAccumulatedStatsRepository accumulatedStatsRepository;
    private final DrivingRecordRepository drivingRecordRepository;

    @Override
    @Transactional
    public DrivingAccumulatedStats integrateAndSave(DrivingSummaryV1 summary) {
        log.info("Integrating driving data: userId={}, drivingId={}", summary.getUserId(), summary.getDrivingId());
        
        // 1. 중복 체크 (이미 저장된 경우 스킵)
        Optional<DrivingAccumulatedStats> existing = accumulatedStatsRepository
                .findByUserIdAndDrivingId(Long.valueOf(summary.getUserId()), summary.getDrivingId());
        
        if (existing.isPresent()) {
            log.debug("Driving stats already exists: drivingId={}", summary.getDrivingId());
            return existing.get();
        }
        
        // 2. DrivingRecord 조회 (추가 정보 획득)
        Optional<DrivingRecord> drivingRecord = drivingRecordRepository.findByDrivingId(summary.getDrivingId());
        
        // 3. 통합 데이터 생성
        DrivingAccumulatedStats stats = buildAccumulatedStats(summary, drivingRecord.orElse(null));
        
        // 4. DrivingRecord도 함께 저장 (주행 카드용)
        if (drivingRecord.isEmpty()) {
            DrivingRecord newRecord = createDrivingRecordFromSummary(summary);
            drivingRecordRepository.save(newRecord);
            log.debug("DrivingRecord created: drivingId={}", summary.getDrivingId());
        }
        
        // 5. DrivingAccumulatedStats 저장
        DrivingAccumulatedStats saved = accumulatedStatsRepository.save(stats);
        
        log.info("Driving stats integrated and saved: id={}, drivingId={}", saved.getId(), summary.getDrivingId());
        return saved;
    }

    /**
     * XADD 데이터와 DrivingRecord를 통합하여 DrivingAccumulatedStats 생성
     */
    private DrivingAccumulatedStats buildAccumulatedStats(DrivingSummaryV1 summary, DrivingRecord record) {
        DrivingAccumulatedStats.DrivingAccumulatedStatsBuilder builder = DrivingAccumulatedStats.builder()
                .userId(Long.valueOf(summary.getUserId()))
                .drivingId(summary.getDrivingId())
                .createdAt(LocalDateTime.now());
        
        // XADD 스트림 필드들
        builder.drivingMinutes(summary.getDrivingMinutes())
               .totalDistance(summary.getTotalDistance())
               .laneChangeCount(summary.getLaneChangeCount())
               .hardBrakeCount(summary.getHardBrakeCount())
               .rapidAccelCount(summary.getRapidAccelCount());
        
        // 시간 정보 (XADD에서 우선, 없으면 DrivingRecord에서)
        LocalDateTime startTime = summary.getStartedAtAsDateTime();
        LocalDateTime endTime = summary.getEndedAtAsDateTime();
        
        if (record != null) {
            // DrivingRecord에서 추가 정보 보완
            builder.avgSpeed(record.getAvgSpeed())
                   .cruiseRatio(record.getCruiseRatio());
            
            // 시간 정보가 XADD에 없으면 DrivingRecord에서 사용
            if (startTime == null) {
                startTime = record.getStartTime();
            }
            if (endTime == null) {
                endTime = record.getEndTime();
            }
        } else {
            log.warn("DrivingRecord not found for drivingId: {}", summary.getDrivingId());
            // XADD 데이터만으로 기본값 설정
            builder.avgSpeed(calculateAvgSpeedFromSummary(summary))
                   .cruiseRatio(0.0); // 기본값
        }
        
        builder.startTime(startTime).endTime(endTime);
        
        return builder.build();
    }

    /**
     * XADD 데이터만으로 평균 속도 계산 (fallback)
     */
    private Double calculateAvgSpeedFromSummary(DrivingSummaryV1 summary) {
        if (summary.getTotalDistance() != null && summary.getDrivingMinutes() != null && summary.getDrivingMinutes() > 0) {
            // km/h = (거리(m) / 1000) / (시간(분) / 60)
            return (summary.getTotalDistance() / 1000.0) / (summary.getDrivingMinutes() / 60.0);
        }
        return 0.0;
    }
    
    /**
     * DrivingSummary로부터 DrivingRecord 생성
     */
    private DrivingRecord createDrivingRecordFromSummary(DrivingSummaryV1 summary) {
        return DrivingRecord.builder()
                .drivingId(summary.getDrivingId())
                .userId(Long.valueOf(summary.getUserId()))
                .startTime(summary.getStartedAtAsDateTime())
                .endTime(summary.getEndedAtAsDateTime())
                .totalDistance(summary.getTotalDistance() != null ? summary.getTotalDistance().doubleValue() : 0.0)
                .avgSpeed(calculateAvgSpeedFromSummary(summary))
                .maxSpeed(0.0) // XADD에 없는 데이터는 기본값
                .minSpeed(0.0)
                .cruiseRatio(0.0) // 추후 S3 분석으로 업데이트 예정
                .laneChangeCount(summary.getLaneChangeCount() != null ? summary.getLaneChangeCount() : 0)
                .hardBrakeCount(summary.getHardBrakeCount() != null ? summary.getHardBrakeCount() : 0)
                .rapidAccelCount(summary.getRapidAccelCount() != null ? summary.getRapidAccelCount() : 0)
                .sharpTurnCount(0) // XADD에 없는 데이터
                .status(SummaryStatus.PROCESSING) // 초기 상태
                .build();
    }
}