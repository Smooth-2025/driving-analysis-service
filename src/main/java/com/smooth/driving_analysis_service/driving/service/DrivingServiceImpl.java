package com.smooth.driving_analysis_service.driving.service;

import com.smooth.driving_analysis_service.driving.dto.response.TodayDrivingResponseDto;
import com.smooth.driving_analysis_service.driving.dto.response.WeeklyDrivingResponseDto;
import com.smooth.driving_analysis_service.driving.dto.result.DrivingAnalysisResultDto;
import com.smooth.driving_analysis_service.driving.dto.request.DrivingCompletionRequestDto;
import com.smooth.driving_analysis_service.timeline.dto.DrivingRecordResponseDto;
import com.smooth.driving_analysis_service.driving.dto.result.EventAnalysisResultDto;
import com.smooth.driving_analysis_service.driving.entity.DrivingRecord;
import com.smooth.driving_analysis_service.driving.entity.SummaryStatus;
import com.smooth.driving_analysis_service.driving.exception.DrivingErrorCode;
import com.smooth.driving_analysis_service.driving.repository.DrivingRecordRepository;
import com.smooth.driving_analysis_service.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class DrivingServiceImpl implements DrivingService {

    private final DrivingRecordRepository drivingRecordRepository;
    private final AthenaQueryService athenaQueryService;

    private final static int TIME_FOR_WAIT = 150000;

    @Async("taskExecutor")
    @Override
    public void summarize(DrivingCompletionRequestDto requestDto) {

        DrivingRecord drivingRecord = DrivingRecord.createInitialRecord(requestDto.getDrivingId(), requestDto.getUserId());

        DrivingRecord savedRecord = drivingRecordRepository.save(drivingRecord);

        processAfterDelay(savedRecord.getId(), requestDto.getDrivingId(), requestDto.getUserId());
    }

    @Async("taskExecutor")
    public void processAfterDelay(Long recordId, String drivingId, Long userId) {
        try {

            Thread.sleep(TIME_FOR_WAIT);

            processAnalysis(recordId, drivingId, userId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("주행 분석 처리 중 인터럽트 발생: drivingId={}", drivingId, e);
        } catch (Exception e) {
            log.error("주행 분석 처리 중 오류 발생: drivingId={}", drivingId, e);
        }
    }

    @Transactional
    public void processAnalysis(Long recordId, String drivingId, Long userId) {
        try {

            DrivingAnalysisResultDto drivingResult = athenaQueryService.getDrivingAnalysis(drivingId);

            EventAnalysisResultDto eventResult = athenaQueryService.getEventAnalysis(drivingId);

            updateDrivingRecord(recordId, drivingResult, eventResult);

        } catch (Exception e) {
            log.error("아테나 쿼리 처리 중 오류 발생: drivingId={}", drivingId, e);
            throw e;
        }
    }

    @Override
    public TodayDrivingResponseDto getTodayDriving(Long userId) {

        List<DrivingRecord> todayDriving = drivingRecordRepository.findByUserIdAndEndTimeToday(userId)
                .stream()
                .filter(record -> record.getStatus().equals(SummaryStatus.COMPLETED))
                .toList();

        if (todayDriving.isEmpty()) {
            return new TodayDrivingResponseDto(0, 0.0, 0);
        }

        double avgCruiseRatio = todayDriving.stream()
                .mapToDouble(DrivingRecord::getCruiseRatio)
                .average()
                .orElse(0.0);

        int cruiseRatioPercent = (int) Math.round(avgCruiseRatio * 100);

        double totalDistance = Math.round(todayDriving.stream()
                .mapToDouble(DrivingRecord::getTotalDistance)
                .sum() / 1000.0 * 10.0) / 10.0;

        int drivingMinutes = (int) todayDriving.stream()
                .mapToLong(record -> ChronoUnit.MINUTES.between(
                        record.getStartTime(),
                        record.getEndTime()
                ))
                .sum();

        TodayDrivingResponseDto drivingResponseDto = new TodayDrivingResponseDto(
                cruiseRatioPercent, totalDistance, drivingMinutes);

        return drivingResponseDto;
    }

    @Override
    public WeeklyDrivingResponseDto getWeeklyDriving(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(6);

        LocalDateTime startOfWeek = weekAgo.atStartOfDay();
        LocalDateTime endOfToday = today.plusDays(1).atStartOfDay();

        // 최근 7일 완료된 주행 데이터 조회
        List<DrivingRecord> weeklyDriving = drivingRecordRepository
                .findByUserIdAndEndTimeBetweenAndStatus(userId, startOfWeek, endOfToday)
                .stream()
                .filter(record -> record.getStatus().equals(SummaryStatus.COMPLETED))
                .toList();

        if (weeklyDriving.isEmpty()) {
            return new WeeklyDrivingResponseDto(0, 0, 0.0, 0, 0, 0, 0, 0.0 );
        }

        double avgCruiseRatio = weeklyDriving.stream()
                .mapToDouble(DrivingRecord::getCruiseRatio)
                .average()
                .orElse(0.0);

        int cruiseRatioPercent = (int) Math.round(avgCruiseRatio * 100);

        double totalDistance = Math.round(weeklyDriving.stream()
                .mapToDouble(DrivingRecord::getTotalDistance)
                .sum() / 1000.0 * 10.0) / 10.0;

        int drivingMinutes = (int) weeklyDriving.stream()
                .mapToLong(record -> ChronoUnit.MINUTES.between(
                        record.getStartTime(),
                        record.getEndTime()
                ))
                .sum();

        int laneChangeCount = weeklyDriving.stream()
                .mapToInt(DrivingRecord::getLaneChangeCount)
                .sum();

        int hardBrakeCount = weeklyDriving.stream()
                .mapToInt(DrivingRecord::getHardBrakeCount)
                .sum();

        int rapidAccelCount = weeklyDriving.stream()
                .mapToInt(DrivingRecord::getRapidAccelCount)
                .sum();

        int sharpTurnCount = weeklyDriving.stream()
                .mapToInt(DrivingRecord::getSharpTurnCount)
                .sum();

        double avgSpeed = Math.round(weeklyDriving.stream()
                .mapToDouble(DrivingRecord::getAvgSpeed)
                .average()
                .orElse(0.0) * 10.0) / 10.0;

        return WeeklyDrivingResponseDto.builder()
                .cruiseRatio(cruiseRatioPercent)
                .drivingMinutes(drivingMinutes)
                .totalDistance(totalDistance)
                .laneChangeCount(laneChangeCount)
                .hardBrakeCount(hardBrakeCount)
                .rapidAccelCount(rapidAccelCount)
                .sharpTurnCount(sharpTurnCount)
                .avgSpeed(avgSpeed)
                .build();
    }

    private void updateDrivingRecord(Long recordId, DrivingAnalysisResultDto drivingResult, EventAnalysisResultDto eventResult) {

        DrivingRecord record = drivingRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(DrivingErrorCode.DRIVING_RECORD_NOT_FOUND,
                "주행 기록을 찾을 수 없습니다: " + recordId));

        record.update(drivingResult, eventResult);
        drivingRecordRepository.save(record);
        log.info("주행 분석 완료: drivingId={}, recordId={}", record.getDrivingId(), recordId);
    }

}
