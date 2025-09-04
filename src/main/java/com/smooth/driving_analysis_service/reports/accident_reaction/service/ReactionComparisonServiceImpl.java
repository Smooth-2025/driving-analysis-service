package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.ReactionComparisonResponseDto;
import com.smooth.driving_analysis_service.reports.accident_reaction.repository.AccidentReactionMetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReactionComparisonServiceImpl implements ReactionComparisonService {
    
    private final AccidentReactionMetricRepository reactionRepo;
    
    @Override
    public ReactionComparisonResponseDto getReactionComparison(Long userId) {
        // 사용자 평균 반응시간
        Double userAvgMs = reactionRepo.findAvgReactionMsByUserId(userId);
        if (userAvgMs == null) {
            throw new IllegalArgumentException("사용자의 반응 데이터가 없습니다: " + userId);
        }
        
        // 전체 평균 반응시간
        Double globalAvgMs = reactionRepo.findGlobalAvgReactionMs();
        if (globalAvgMs == null) {
            throw new IllegalStateException("전체 사용자 반응 데이터가 없습니다");
        }
        
        // 사용자 알림 수 조회
        Long userAlertCount = reactionRepo.countByUserId(userId);
        
        // 사용자 반응 비율 조회
        Map<String, Object> userReactionStats = reactionRepo.getUserReactionStats(userId);
        
        // 비교 결과 계산
        boolean fasterThanAverage = userAvgMs < globalAvgMs;
        double deltaSec = (globalAvgMs - userAvgMs) / 1000.0; // 차이 (초)
        
        // 전체 사용자 중 몇 %보다 빠른지 계산
        Long totalUsers = reactionRepo.countDistinctUsersWithReaction();
        Long slowerUsers = reactionRepo.countDistinctUsersSlowerThan(userAvgMs.intValue());
        
        double fasterThanPercent = 0.0;
        if (totalUsers > 0) {
            fasterThanPercent = (slowerUsers.doubleValue() / totalUsers.doubleValue()) * 100.0;
        }
        
        // 성능 등급 결정
        String performance = determinePerformance(fasterThanPercent);
        
        // DTO 생성 (UI 요구사항에 맞춰)
        var userStats = ReactionComparisonResponseDto.UserReactionStats.builder()
                .avgReactionMs(userAvgMs)
                .avgReactionSec(Math.round(userAvgMs / 1000.0 * 10.0) / 10.0) // 소수점 1자리
                .receivedAlertCount(userAlertCount.intValue())
                .brakeOrStopRatio(getDoubleValue(userReactionStats, "brakeRatio"))
                .avoidRatio(getDoubleValue(userReactionStats, "avoidRatio"))
                .build();
                
        var globalStats = ReactionComparisonResponseDto.GlobalReactionStats.builder()
                .avgReactionMs(globalAvgMs)
                .avgReactionSec(Math.round(globalAvgMs / 1000.0 * 10.0) / 10.0) // 소수점 1자리
                .build();
                
        var comparison = ReactionComparisonResponseDto.ComparisonResult.builder()
                .fasterThanAverage(fasterThanAverage)
                .fasterThanPercent(Math.round(fasterThanPercent * 10.0) / 10.0) // 소수점 1자리
                .performance(performance)
                .deltaSec(Math.round(deltaSec * 10.0) / 10.0) // 차이 (초)
                .build();
        
        // UI 형식 데이터 생성
        var benchmark = ReactionComparisonResponseDto.Benchmark.builder()
                .avgReactionSecOfAllUsers(globalStats.getAvgReactionSec())
                .deltaSec(-deltaSec) // 음수면 더 빠름
                .build();
                
        var chart = ReactionComparisonResponseDto.Chart.builder()
                .labels(new String[]{"일반 운전자", "내 주행"})
                .valuesSec(new Double[]{globalStats.getAvgReactionSec(), userStats.getAvgReactionSec()})
                .build();
        
        return ReactionComparisonResponseDto.builder()
                // UI 형식 (메인)
                .reportId("report_" + userId) // 임시 reportId
                .receivedAlertCount(userStats.getReceivedAlertCount())
                .avgReactionSec(userStats.getAvgReactionSec())
                .brakeOrStopRatio(userStats.getBrakeOrStopRatio())
                .avoidRatio(userStats.getAvoidRatio())
                .benchmark(benchmark)
                .chart(chart)
                // 기존 구조 (하위 호환성)
                .userStats(userStats)
                .globalStats(globalStats)
                .comparison(comparison)
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