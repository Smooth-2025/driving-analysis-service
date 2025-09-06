package com.smooth.driving_analysis_service.reports.accident_reaction.dto.response;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReactionComparisonResponseDto {
    
    private String reportId;           // 리포트 식별용
    private Integer receivedAlertCount; // 수신한 사고 알림 수
    private Double avgReactionSec;     // 내 평균 반응 시간 (초)
    private Double brakeOrStopRatio;   // 감속/정지 반응 비율
    private Double avoidRatio;         // 우회 반응 비율
    private Benchmark benchmark;       // 평균 사용자 대비
    private Chart chart;               // 차트 데이터
    
    // 기존 구조도 유지 (하위 호환성)
    private UserReactionStats userStats;
    private GlobalReactionStats globalStats;
    private ComparisonResult comparison;
    
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UserReactionStats {
        private Double avgReactionMs;      // 사용자 평균 반응시간 (밀리초)
        private Double avgReactionSec;     // 사용자 평균 반응시간 (초)
        private Integer receivedAlertCount; // 수신한 알림 수
        private Double brakeOrStopRatio;   // 감속/정지 비율
        private Double avoidRatio;         // 우회 비율
    }
    
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class GlobalReactionStats {
        private Double avgReactionMs;      // 전체 평균 반응시간 (밀리초)
        private Double avgReactionSec;     // 전체 평균 반응시간 (초)
    }
    
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ComparisonResult {
        private Boolean fasterThanAverage; // 평균보다 빠른지
        private Double fasterThanPercent;  // 전체 사용자의 몇 %보다 빠른지
        private String performance;        // "빠름", "보통", "느림"
        private Double deltaSec;           // 차이 (초)
    }
    
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Benchmark {
        private Double avgReactionSecOfAllUsers; // 전체 사용자 평균 (초)
        private Double deltaSec;                 // 차이 (초, 음수면 더 빠름)
    }
    
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Chart {
        private String[] labels;    // ["일반 운전자", "내 주행"]
        private Double[] valuesSec; // [151, 90]
    }
}