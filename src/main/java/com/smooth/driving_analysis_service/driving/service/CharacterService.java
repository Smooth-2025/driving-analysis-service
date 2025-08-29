package com.smooth.driving_analysis_service.driving.service;

import com.smooth.driving_analysis_service.driving.dto.response.CharacterBulkResponseDto;
import com.smooth.driving_analysis_service.driving.dto.response.UserCharacterResponseDto;

public interface CharacterService {

    CharacterBulkResponseDto getAllUsersCharacter(boolean hasCharacter);

    UserCharacterResponseDto getUserCharacter(Long userId);
}
