package com.smooth.driving_analysis_service.reports.milestone.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record MilestoneReadToggleRequest(
        @NotNull Boolean read
) {}
