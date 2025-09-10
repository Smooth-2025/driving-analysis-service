package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import com.smooth.driving_analysis_service.global.util.AuthenticationUtils;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.AccidentReactionBasicMetricsDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.AccidentReactionBenchmarkDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.AccidentReactionReportResponseDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.repository.AccidentReactionMetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccidentReactionReportServiceImpl implements AccidentReactionReportService {
    
    private final AccidentReactionMetricRepository accidentReactionMetricRepository;
    
    @Override
    @Transactional(readOnly = true)
    public AccidentReactionBasicMetricsDto getBasicMetrics(String reportId) {
        try {
            Long userId = AuthenticationUtils.getCurrentUserIdOrThrow();
            Long reportIdLong = Long.parseLong(reportId);
            
            log.info("Getting accident reaction basic metrics - userId: {}, reportId: {}", userId, reportIdLong);
            
            // 먼저 기본 쿼리 시도 (사용자별)
            Object[] result = accidentReactionMetricRepository.getBasicMetricsByUserIdAndReportId(userId, reportIdLong);
            log.debug("Primary query result for reportId {}: {}", reportId, 
                    result != null ? java.util.Arrays.toString(result) : "null");
            
            // 결과가 없거나 모든 값이 0인 경우 대안 쿼리 시도
            if (result == null || result.length < 4 || isEmptyResult(result)) {
                log.warn("No metrics found with primary query for reportId: {}, trying alternative query", reportId);
                
                // null drivingId 개수도 확인
                Long nullCount = accidentReactionMetricRepository.countNullDrivingIdsByReportId(reportIdLong);
                log.info("Number of AlertRenderEvents with null drivingId for reportId {}: {}", reportId, nullCount);
                
                result = accidentReactionMetricRepository.getBasicMetricsByReportIdAlternative(reportIdLong);
                log.debug("Alternative query result for reportId {}: {}", reportId, 
                        result != null ? java.util.Arrays.toString(result) : "null");
            }
            
            if (result == null || result.length < 4) {
                log.warn("No metrics found for reportId: {} with both queries", reportId);
                return createEmptyMetrics();
            }
            
            // 쿼리 결과 파싱 (매우 안전한 캐스팅)
            // result는 [receivedAlertCount, avgReactionSec, brakeOrStopRatio, avoidRatio] 순서
            Long receivedAlertCount = 0L;
            Double avgReactionSec = 0.0;
            Double brakeOrStopRatio = 0.0;
            Double avoidRatio = 0.0;
            
            try {
                if (result[0] != null) {
                    receivedAlertCount = Long.valueOf(result[0].toString());
                }
                if (result[1] != null) {
                    avgReactionSec = Double.valueOf(result[1].toString());
                }
                if (result[2] != null) {
                    brakeOrStopRatio = Double.valueOf(result[2].toString());
                }
                if (result[3] != null) {
                    avoidRatio = Double.valueOf(result[3].toString());
                }
            } catch (Exception e) {
                log.error("Error parsing query result for reportId {}: {}", reportId, e.getMessage());
                return createEmptyMetrics();
            }
            
            log.debug("Basic metrics for reportId {}: count={}, avgReaction={}, brakeRatio={}, avoidRatio={}", 
                    reportId, receivedAlertCount, avgReactionSec, brakeOrStopRatio, avoidRatio);
            
            return AccidentReactionBasicMetricsDto.builder()
                    .receivedAlertCount(receivedAlertCount.intValue())
                    .avgReactionSec(avgReactionSec)
                    .brakeOrStopRatio(brakeOrStopRatio)
                    .avoidRatio(avoidRatio)
                    .build();
                    
        } catch (NumberFormatException e) {
            log.error("Invalid reportId format: {}", reportId, e);
            return createEmptyMetrics();
        } catch (ClassCastException e) {
            log.error("Failed to cast query result for reportId: {}", reportId, e);
            return createEmptyMetrics();
        } catch (Exception e) {
            log.error("Failed to get basic metrics for reportId: {}", reportId, e);
            return createEmptyMetrics();
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public AccidentReactionBenchmarkDto getBenchmark(String reportId) {
        try {
            // 1. 개인 평균 반응시간 조회 (Task 1에서 계산된 값 재사용)
            AccidentReactionBasicMetricsDto basicMetrics = getBasicMetrics(reportId);
            Double myAvgReactionSec = basicMetrics.getAvgReactionSec();
            
            // 2. 전체 사용자 평균 반응시간 조회
            Double globalAvgReactionSec = accidentReactionMetricRepository.getGlobalAverageReactionTime();
            
            // 기본값 설정 (데이터가 없는 경우)
            if (myAvgReactionSec == null) myAvgReactionSec = 0.0;
            if (globalAvgReactionSec == null) globalAvgReactionSec = 2.0; // 기본 벤치마크 2초
            
            // 3. deltaSec 계산: 일반 평균 - 내 평균 (음수=더 느림, 양수=더 빠름)
            Double deltaSec = globalAvgReactionSec - myAvgReactionSec;
            
            // 4. 차트 데이터 생성
            AccidentReactionBenchmarkDto.ChartDto chart = AccidentReactionBenchmarkDto.ChartDto.builder()
                    .labels(new String[]{"일반 운전자", "내 주행"})
                    .valuesSec(new Double[]{globalAvgReactionSec, myAvgReactionSec})
                    .build();
            
            return AccidentReactionBenchmarkDto.builder()
                    .deltaSec(Math.round(deltaSec * 10.0) / 10.0) // 소수점 1자리 반올림
                    .chart(chart)
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed to get benchmark for reportId: {}", reportId, e);
            return createEmptyBenchmark();
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public AccidentReactionReportResponseDto getFullReport(String reportId) {
        AccidentReactionBasicMetricsDto basicMetrics = getBasicMetrics(reportId);
        AccidentReactionBenchmarkDto benchmark = getBenchmark(reportId);
        
        return AccidentReactionReportResponseDto.builder()
                .reportId(reportId)
                .receivedAlertCount(basicMetrics.getReceivedAlertCount())
                .avgReactionSec(basicMetrics.getAvgReactionSec())
                .brakeOrStopRatio(basicMetrics.getBrakeOrStopRatio())
                .avoidRatio(basicMetrics.getAvoidRatio())
                .benchmark(benchmark)
                .build();
    }
    
    private AccidentReactionBasicMetricsDto createEmptyMetrics() {
        return AccidentReactionBasicMetricsDto.builder()
                .receivedAlertCount(0)
                .avgReactionSec(0.0)
                .brakeOrStopRatio(0.0)
                .avoidRatio(0.0)
                .build();
    }
    
    private AccidentReactionBenchmarkDto createEmptyBenchmark() {
        AccidentReactionBenchmarkDto.ChartDto chart = AccidentReactionBenchmarkDto.ChartDto.builder()
                .labels(new String[]{"일반 운전자", "내 주행"})
                .valuesSec(new Double[]{2.0, 0.0})
                .build();
                
        return AccidentReactionBenchmarkDto.builder()
                .deltaSec(2.0) // 기본값: 일반 2초 - 내 0초 = 2초 빠름
                .chart(chart)
                .build();
    }
    
    /**
     * 쿼리 결과가 비어있는지 확인 (모든 값이 0이거나 null인 경우)
     */
    private boolean isEmptyResult(Object[] result) {
        if (result == null || result.length < 4) {
            return true;
        }
        
        // receivedAlertCount가 0이면 빈 결과로 간주
        try {
            Long alertCount = result[0] != null ? Long.valueOf(result[0].toString()) : 0L;
            return alertCount == 0;
        } catch (Exception e) {
            log.debug("Error checking empty result: {}", e.getMessage());
            return true;
        }
    }
}