package com.smooth.driving_analysis_service.reports.milestone.dto.result;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MilestoneReportResult {

    private Long reportId;       // milestone_report.id
    private int totalTrips;      // 전체 주행 수
    private double averageSpeed; // 평균 속도
    private double averageDistance; // 평균 주행 거리
    private double cruiseRatio;     // 평균 정속 주행률

    // TODO: 이벤트 통계, 시간 패턴, DNA 프로파일링 결과 등 추가 예정
}
