package com.smooth.driving_analysis_service.reports.behavior.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TotalCountsDto {
    private Integer hardBrake;
    private Integer rapidAccel;
    private Integer laneChange;
    private Integer total;
    
    public static TotalCountsDto of(Integer hardBrake, Integer rapidAccel, Integer laneChange) {
        return TotalCountsDto.builder()
                .hardBrake(hardBrake)
                .rapidAccel(rapidAccel)
                .laneChange(laneChange)
                .total(hardBrake + rapidAccel + laneChange)
                .build();
    }
}