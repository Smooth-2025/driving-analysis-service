package com.smooth.driving_analysis_service.driving.service;

import com.smooth.driving_analysis_service.driving.dto.result.DrivingAnalysisResultDto;
import com.smooth.driving_analysis_service.driving.dto.request.DrivingCompletionRequestDto;
import com.smooth.driving_analysis_service.driving.dto.response.DrivingRecordResponseDto;
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
    public CompletableFuture<DrivingRecordResponseDto> summarize(DrivingCompletionRequestDto requestDto) {

        DrivingRecord drivingRecord = DrivingRecord.createInitialRecord(requestDto.getDrivingId(), requestDto.getUserId());

        DrivingRecord savedRecord = drivingRecordRepository.save(drivingRecord);

        processAfterDelay(savedRecord.getId(), requestDto.getDrivingId(), requestDto.getUserId());

        return CompletableFuture.completedFuture(DrivingRecordResponseDto.from(savedRecord));
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

    private void updateDrivingRecord(Long recordId, DrivingAnalysisResultDto drivingResult, EventAnalysisResultDto eventResult) {

        DrivingRecord record = drivingRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(DrivingErrorCode.DRIVING_RECORD_NOT_FOUND,
                "주행 기록을 찾을 수 없습니다: " + recordId));

        record.update(drivingResult, eventResult);
        drivingRecordRepository.save(record);
        log.info("주행 분석 완료: drivingId={}, recordId={}", record.getDrivingId(), recordId);
    }

}
