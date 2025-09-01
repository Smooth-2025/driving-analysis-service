package com.smooth.driving_analysis_service.reports.milestone.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record MilestoneStampUpdateRequest(
        @Min(0) @Max(15) int numberOfDriving
) {}
