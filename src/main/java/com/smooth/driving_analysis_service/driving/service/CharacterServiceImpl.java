package com.smooth.driving_analysis_service.driving.service;

import com.smooth.driving_analysis_service.driving.dto.request.DrivingCharacterAnalysisRequestDto;
import com.smooth.driving_analysis_service.driving.dto.response.DrivingCharacterResponseDto;
import com.smooth.driving_analysis_service.driving.dto.response.UserCharacterBulkResponseDto;
import com.smooth.driving_analysis_service.driving.dto.response.UserCharacterResponseDto;
import com.smooth.driving_analysis_service.driving.dto.result.DrivingCharacterAnalysisResultDto;
import com.smooth.driving_analysis_service.driving.entity.DrivingCharacter;
import com.smooth.driving_analysis_service.driving.entity.DrivingCharacterType;
import com.smooth.driving_analysis_service.driving.entity.DrivingRecord;
import com.smooth.driving_analysis_service.driving.entity.UserDrivingState;
import com.smooth.driving_analysis_service.driving.exception.DrivingErrorCode;
import com.smooth.driving_analysis_service.driving.repository.DrivingCharacterRepository;
import com.smooth.driving_analysis_service.driving.repository.DrivingRecordRepository;
import com.smooth.driving_analysis_service.driving.repository.UserDrivingStateRepository;
import com.smooth.driving_analysis_service.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CharacterServiceImpl implements CharacterService {

    private final DrivingCharacterRepository drivingCharacterRepository;
    private final UserDrivingStateRepository userDrivingStateRepository;
    private final DrivingRecordRepository drivingRecordRepository;
    private final BedrockService bedrockService;

    @Override
    public DrivingCharacterResponseDto getCurrentDrivingCharacter(Long userId) {

        DrivingCharacter drivingCharacter = drivingCharacterRepository.findFirstByUserIdAndCharacterTypeNotOrderByCreatedAtDesc(userId, DrivingCharacterType.NONE)
                .orElse(DrivingCharacter.createNoneDrivingCharacter());
        UserDrivingState userDrivingState = userDrivingStateRepository.findByUserId(userId)
                .orElse(UserDrivingState.createInitialUserDrivingState(userId));

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

    @Transactional
    @Override
    public void analyzeCharacter(Long userId, Long toRecordId) {

        DrivingCharacter drivingCharacter =
                drivingCharacterRepository.findFirstByUserIdAndCharacterTypeOrderByCreatedAtDesc(
                userId, DrivingCharacterType.NONE)
                        .orElse(DrivingCharacter.createInitialDrivingCharacter(userId, toRecordId));

        DrivingCharacterAnalysisRequestDto requestDto = prepareData(drivingCharacter.getFromRecordId(), toRecordId, userId);

        DrivingCharacterAnalysisResultDto result = bedrockService.invokeModel(requestDto);
        drivingCharacter.update(result, toRecordId, requestDto.getTotalDistanceKm());

        UserDrivingState userDrivingState = userDrivingStateRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(DrivingErrorCode.DRIVING_STATE_NOT_FOUND, "업데이트할 주행 상태가 없습니다."));

        userDrivingState.reset(userDrivingState.getPendingDistanceKm(),toRecordId,drivingCharacter.getCharacterType());
    }

    private DrivingCharacterAnalysisRequestDto prepareData(Long fromRecordId, Long toRecordId, Long userId) {

        List<DrivingRecord> drivingRecords = drivingRecordRepository.findRecordsByIdRangeAndUserId(fromRecordId, toRecordId, userId);

        if (drivingRecords.isEmpty()) {
            throw new BusinessException(DrivingErrorCode.DRIVING_RECORD_NOT_FOUND_IN_PERIOD, "100km 동안의 주행 기록이 없습니다.");
        }

        // 총 거리 (km)
        double totalDistanceM = drivingRecords.stream()
                .mapToDouble(DrivingRecord::getTotalDistance)
                .sum();

        double totalDistanceKm = Math.round(totalDistanceM / 1000.0 * 10.0) / 10.0;

        // 총 운전 시간 (분)
        int drivingMinutes = (int) drivingRecords.stream()
                .mapToLong(record -> ChronoUnit.MINUTES.between(
                        record.getStartTime(),
                        record.getEndTime()
                ))
                .sum();

        // 평균 속도 계산
        double avgSpeed = drivingRecords.stream()
                .mapToDouble(DrivingRecord::getAvgSpeed)
                .average()
                .orElse(0.0);

        // 최대 속도
        double maxSpeed = drivingRecords.stream()
                .mapToDouble(DrivingRecord::getMaxSpeed)
                .max()
                .orElse(0.0);

        // cruiseRatio
        double cruiseRatio = drivingRecords.stream()
                .mapToDouble(DrivingRecord::getCruiseRatio)
                .average()
                .orElse(0.0);

        // 실제 발생 횟수들
        int laneChangeCount = drivingRecords.stream()
                .mapToInt(DrivingRecord::getLaneChangeCount)
                .sum();

        int rapidAccelCount = drivingRecords.stream()
                .mapToInt(DrivingRecord::getRapidAccelCount)
                .sum();

        int hardBrakeCount = drivingRecords.stream()
                .mapToInt(DrivingRecord::getHardBrakeCount)
                .sum();

        int sharpTurnCount = drivingRecords.stream()
                .mapToInt(DrivingRecord::getSharpTurnCount)
                .sum();

        int totalAggressiveCount = rapidAccelCount + hardBrakeCount + sharpTurnCount;

        return DrivingCharacterAnalysisRequestDto.builder()
                .cruiseRatio(cruiseRatio)
                .drivingMinutes(drivingMinutes)
                .totalDistanceKm(totalDistanceKm)
                .avgSpeed(avgSpeed)
                .maxSpeed(maxSpeed)
                .laneChangeCount(laneChangeCount)
                .rapidAccelCount(rapidAccelCount)
                .hardBrakeCount(hardBrakeCount)
                .sharpTurnCount(sharpTurnCount)
                .totalAggressiveCount(totalAggressiveCount)
                .build();
    }
}
