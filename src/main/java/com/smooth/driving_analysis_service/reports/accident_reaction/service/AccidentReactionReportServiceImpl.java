package com.smooth.driving_analysis_service.reports.accident_reaction.service;

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
            Long reportIdLong = Long.parseLong(reportId);
            Object[] result = accidentReactionMetricRepository.getBasicMetricsByReportId(reportIdLong);
            
            if (result == null || result.length == 0) {
                return createEmptyMetrics();
            }
            
            // 쿼리 결과 파싱
            Long receivedAlertCount = (Long) result[0];
            Double avgReactionSec = (Double) result[1];
            Double brakeOrStopRatio = (Double) result[2];
            Double avoidRatio = (Double) result[3];
            
            return AccidentReactionBasicMetricsDto.builder()
                    .receivedAlertCount(receivedAlertCount != null ? receivedAlertCount.intValue() : 0)
                    .avgReactionSec(avgReactionSec != null ? avgReactionSec : 0.0)
                    .brakeOrStopRatio(brakeOrStopRatio != null ? brakeOrStopRatio : 0.0)
                    .avoidRatio(avoidRatio != null ? avoidRatio : 0.0)
                    .build();
                    
        } catch (NumberFormatException e) {
            log.error("Invalid reportId format: {}", reportId, e);
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
                .benchmark(benchmark)  // Task 2 추가
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
}