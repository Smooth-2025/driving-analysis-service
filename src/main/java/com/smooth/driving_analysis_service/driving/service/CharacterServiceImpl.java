package com.smooth.driving_analysis_service.driving.service;

import com.smooth.driving_analysis_service.driving.dto.response.UserCharacterBulkResponseDto;
import com.smooth.driving_analysis_service.driving.dto.response.UserCharacterResponseDto;
import com.smooth.driving_analysis_service.driving.entity.DrivingCharacter;
import com.smooth.driving_analysis_service.driving.entity.DrivingCharacterType;
import com.smooth.driving_analysis_service.driving.exception.DrivingErrorCode;
import com.smooth.driving_analysis_service.driving.repository.DrivingCharacterRepository;
import com.smooth.driving_analysis_service.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CharacterServiceImpl implements CharacterService {

    private final DrivingCharacterRepository drivingCharacterRepository;

    @Override
    public UserCharacterBulkResponseDto getAllUsersCharacter(boolean hasCharacter) {
        List<DrivingCharacter> allUsers = drivingCharacterRepository.findAll();

        if (hasCharacter) {
            allUsers = allUsers.stream()
                    .filter(c -> c.getCharacterType() != DrivingCharacterType.NONE)
                    .toList();
        }

        List<UserCharacterResponseDto> userCharacterList = allUsers.stream()
                .map(c -> new UserCharacterResponseDto(c.getUserId(), c.getCharacterType().toString()))
                .toList();

        return UserCharacterBulkResponseDto.of(userCharacterList);

    }

    @Override
    public UserCharacterResponseDto getUserCharacter(Long userId) {
        Optional<DrivingCharacter> drivingCharacter = drivingCharacterRepository.findByUserId(userId);
        if (drivingCharacter.isEmpty() || drivingCharacter.get().getCharacterType() == DrivingCharacterType.NONE) {
            throw new BusinessException(DrivingErrorCode.CHARACTER_NOT_FOUND, "No character for userId=" + userId);
        }
        return new UserCharacterResponseDto(drivingCharacter.get().getUserId(), drivingCharacter.get().getCharacterType().toString());
    }
}
