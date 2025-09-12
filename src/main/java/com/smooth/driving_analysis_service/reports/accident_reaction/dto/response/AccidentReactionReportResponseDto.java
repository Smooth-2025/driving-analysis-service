package com.smooth.driving_analysis_service.reports.accident_reaction.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AccidentReactionReportResponseDto {
    private String reportId;          // 리포트 식별용
    private int receivedAlertCount;   // 수신한 사고 알림 수
    private double avgReactionSec;    // 내 평균 반응 시간 (초)
    private double brakeOrStopRatio;  // 감속/정지 반응 비율
    private double avoidRatio;        // 우회 반응 비율
    private Benchmark benchmark;      // 벤치마크 정보

    @Getter
    @Builder
    public static class Benchmark {
        private int deltaSec;         // 평균 반응 시간 차이 (초)
        private Chart chart;          // 차트 데이터
    }

    @Getter
    @Builder
    public static class Chart {
        private List<String> labels;  // ["일반 운전자", "내 주행"]
        private List<Integer> valuesSec; // [56, 55]
    }
}