package com.smooth.driving_analysis_service.timepattern.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TimePatternGridCellDto {
    private int dow;          // 0=월~6=일
    private int hour;         // 0~23
    private long trips;
    private long hardBrakes;
    private long rapidAccels;
    private long laneChanges;
}
