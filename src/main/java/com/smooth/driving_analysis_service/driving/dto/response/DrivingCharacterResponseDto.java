package com.smooth.driving_analysis_service.driving.dto.response;

import com.smooth.driving_analysis_service.driving.entity.DrivingCharacter;
import com.smooth.driving_analysis_service.driving.entity.UserDrivingState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DrivingCharacterResponseDto {
    private String characterType;
    private String characterTrait;
    private String description;
    private String improvementSuggestions;
    private String speedPreference;
    private int currentDistance;
    private int remainingDistance;

    public static DrivingCharacterResponseDto from(DrivingCharacter character, UserDrivingState state) {

        int currentDistance = state.getPendingDistanceKm() >= 100.0 ? 100 : (int) state.getPendingDistanceKm();

        int remainingDistance = 100 - currentDistance;

        return DrivingCharacterResponseDto.builder()
                .characterType(character.getCharacterType().toString())
                .characterTrait(character.getCharacterTrait())
                .description(character.getDescription())
                .improvementSuggestions(character.getImprovementSuggestions())
                .speedPreference(character.getSpeedPreference())
                .currentDistance(currentDistance)
                .remainingDistance(remainingDistance)
                .build();
    }
}
