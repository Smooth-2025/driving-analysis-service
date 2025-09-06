package com.smooth.driving_analysis_service.driving.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DrivingCharacterAnalysisResultDto {
    private String characterType;
    private int confidenceScore;
    private String characterTrait;
    private String drivingStyle;
    private String speedPreference;
    private String improvementSuggestions;
    private String personalityDescription;
}
