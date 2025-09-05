package com.smooth.driving_analysis_service.reports.behavior.dto.result;

import com.smooth.driving_analysis_service.reports.behavior.entity.BehaviorType;
import com.smooth.driving_analysis_service.reports.behavior.entity.DiffDirection;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BehaviorDiffResultDto {
    private BehaviorType behavior;
    private int prev;
    private int curr;
    private int diff;
    private String pct;
    private DiffDirection direction; // INCREASE / DECREASE / FLAT
}
