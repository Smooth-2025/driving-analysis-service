package com.smooth.driving_analysis_service.reports.behavior.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeekdayActionsDto {
    private WeekdayActionDto hardBrake;
    private WeekdayActionDto rapidAccel;
    private WeekdayActionDto laneChange;
}