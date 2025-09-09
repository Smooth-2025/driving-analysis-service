package com.smooth.driving_analysis_service.reports.milestone.dto.request;

import jakarta.validation.constraints.NotNull;

public record MilestoneReadToggleRequestDto(
        @NotNull Boolean read
) {}
