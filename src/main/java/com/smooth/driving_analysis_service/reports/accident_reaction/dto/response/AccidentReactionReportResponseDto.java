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
<<<<<<< HEAD
    private final AccidentReactionBenchmarkDto benchmark;  // Task 2 추가
=======
>>>>>>> 437e3459a657278af0bed63445b86a1591a7cc39
}