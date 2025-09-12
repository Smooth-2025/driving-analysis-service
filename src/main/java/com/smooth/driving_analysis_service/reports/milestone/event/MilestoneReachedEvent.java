package com.smooth.driving_analysis_service.reports.milestone.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MilestoneReachedEvent {
    private final Long userId;
    private final String milestone;
}