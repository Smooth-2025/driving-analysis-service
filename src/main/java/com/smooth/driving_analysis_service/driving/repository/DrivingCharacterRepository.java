package com.smooth.driving_analysis_service.driving.repository;

import com.smooth.driving_analysis_service.driving.entity.DrivingCharacter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DrivingCharacterRepository extends JpaRepository<DrivingCharacter, Long> {

    Optional<DrivingCharacter> findByUserId(Long userId);
}
