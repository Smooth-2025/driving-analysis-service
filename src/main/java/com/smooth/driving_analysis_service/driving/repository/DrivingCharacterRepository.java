package com.smooth.driving_analysis_service.driving.repository;

import com.smooth.driving_analysis_service.driving.entity.DrivingCharacter;
import com.smooth.driving_analysis_service.driving.entity.DrivingCharacterType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DrivingCharacterRepository extends JpaRepository<DrivingCharacter, Long> {

    Optional<DrivingCharacter> findFirstByUserIdAndCharacterTypeNotOrderByCreatedAtDesc(
            Long userId, DrivingCharacterType type);

    Optional<DrivingCharacter> findFirstByUserIdAndCharacterTypeOrderByCreatedAtDesc(
            Long userId, DrivingCharacterType type);

    Optional<DrivingCharacter> findFirstByUserIdOrderByCreatedAtDesc(Long userId);
}
