package com.smooth.driving_analysis_service.reports.accident_reaction.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccidentReactionReportResponseDto {
    private final String reportId;
    private final Integer receivedAlertCount;
    private final Double avgReactionSec;
    private final Double brakeOrStopRatio;
    private final Double avoidRatio;
    private final AccidentReactionBenchmarkDto benchmark;  // Task 2 추가
}