package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.AccidentReactionBasicMetricsDto;
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
    public AccidentReactionReportResponseDto getFullReport(String reportId) {
        AccidentReactionBasicMetricsDto basicMetrics = getBasicMetrics(reportId);
        
        return AccidentReactionReportResponseDto.builder()
                .reportId(reportId)
                .receivedAlertCount(basicMetrics.getReceivedAlertCount())
                .avgReactionSec(basicMetrics.getAvgReactionSec())
                .brakeOrStopRatio(basicMetrics.getBrakeOrStopRatio())
                .avoidRatio(basicMetrics.getAvoidRatio())
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
}