package com.smooth.driving_analysis_service.driving.service;

import com.smooth.driving_analysis_service.driving.dto.response.DrivingCharacterResponseDto;
import com.smooth.driving_analysis_service.driving.dto.response.UserCharacterBulkResponseDto;
import com.smooth.driving_analysis_service.driving.dto.response.UserCharacterResponseDto;

public interface CharacterService {

    DrivingCharacterResponseDto getCurrentDrivingCharacter(Long userId);

    UserCharacterBulkResponseDto getAllUsersCharacter(boolean hasCharacter);

    UserCharacterResponseDto getUserCharacter(Long userId);

    void analyzeCharacter(Long userId, Long recordId);
}
