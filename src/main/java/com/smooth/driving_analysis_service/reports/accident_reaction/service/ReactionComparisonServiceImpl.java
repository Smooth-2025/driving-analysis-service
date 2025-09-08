package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.ReactionComparisonResponseDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.repository.AccidentReactionMetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReactionComparisonServiceImpl implements ReactionComparisonService {
    
    private final AccidentReactionMetricRepository reactionRepo;
    
    @Override
    public ReactionComparisonResponseDto getReactionComparison(Long userId) {
        // TODO: Task 2에서 구현 예정
        // 현재는 기본값 반환
        return ReactionComparisonResponseDto.builder()
                .reportId("report_" + userId)
                .receivedAlertCount(0)
                .avgReactionSec(0.0)
                .brakeOrStopRatio(0.0)
                .avoidRatio(0.0)
                .build();
    }
    
    private double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }
    
    private String determinePerformance(double fasterThanPercent) {
        if (fasterThanPercent >= 80.0) {
            return "매우 빠름";
        } else if (fasterThanPercent >= 60.0) {
            return "빠름";
        } else if (fasterThanPercent >= 40.0) {
            return "보통";
        } else if (fasterThanPercent >= 20.0) {
            return "느림";
        } else {
            return "매우 느림";
        }
    }
}