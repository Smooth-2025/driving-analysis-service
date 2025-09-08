package com.smooth.driving_analysis_service.driving.service;

import com.smooth.driving_analysis_service.driving.dto.response.DrivingCharacterResponseDto;
import com.smooth.driving_analysis_service.driving.dto.response.UserCharacterBulkResponseDto;
import com.smooth.driving_analysis_service.driving.dto.response.UserCharacterResponseDto;
import com.smooth.driving_analysis_service.driving.entity.DrivingCharacter;
import com.smooth.driving_analysis_service.driving.entity.DrivingCharacterType;
import com.smooth.driving_analysis_service.driving.entity.UserDrivingState;
import com.smooth.driving_analysis_service.driving.exception.DrivingErrorCode;
import com.smooth.driving_analysis_service.driving.repository.DrivingCharacterRepository;
import com.smooth.driving_analysis_service.driving.repository.UserDrivingStateRepository;
import com.smooth.driving_analysis_service.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CharacterServiceImpl implements CharacterService {

    private final DrivingCharacterRepository drivingCharacterRepository;
    private final UserDrivingStateRepository userDrivingStateRepository;

    @Override
    public DrivingCharacterResponseDto getCurrentDrivingCharacter(Long userId) {

        DrivingCharacter drivingCharacter = drivingCharacterRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new BusinessException(
                        DrivingErrorCode.CHARACTER_NOT_FOUND, "No character for userId=" + userId
                ));
        UserDrivingState userDrivingState = userDrivingStateRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(
                        DrivingErrorCode.DRIVING_STATE_NOT_FOUND, "No driving state  for userId=" + userId
                ));

        return DrivingCharacterResponseDto.from(drivingCharacter, userDrivingState);
    }

    @Override
    public UserCharacterBulkResponseDto getAllUsersCharacter(boolean hasCharacter) {
        List<UserDrivingState> allUsers = userDrivingStateRepository.findAll();

        if (hasCharacter) {
            allUsers = allUsers.stream()
                    .filter(state -> state.getCurrentCharacterType() != DrivingCharacterType.NONE)
                    .toList();
        }

        List<UserCharacterResponseDto> userCharacterList = allUsers.stream()
                .map(c -> new UserCharacterResponseDto(c.getUserId(), c.getCurrentCharacterType().toString()))
                .toList();

        return UserCharacterBulkResponseDto.of(userCharacterList);

    }

    @Override
    public UserCharacterResponseDto getUserCharacter(Long userId) {
        Optional<UserDrivingState> drivingCharacter = userDrivingStateRepository.findByUserId(userId);

        if (drivingCharacter.isEmpty() || drivingCharacter.get().getCurrentCharacterType() == DrivingCharacterType.NONE) {
            throw new BusinessException(DrivingErrorCode.CHARACTER_NOT_FOUND, "No character for userId=" + userId);
        }

        return new UserCharacterResponseDto(userId, drivingCharacter.get().getCurrentCharacterType().toString());
    }

    @Override
    public void analyzeCharacter(Long userId, Long recordId) {
        // TODO: 캐릭터 분석 로직 구현
        // 현재는 빈 구현으로 컴파일 에러만 해결
        // 실제 구현이 필요한 경우 BedrockService와 연동하여 분석 수행
    }
}