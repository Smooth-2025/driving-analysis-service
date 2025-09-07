package com.smooth.driving_analysis_service.driving.repository;

import com.smooth.driving_analysis_service.driving.entity.UserDrivingState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDrivingStateRepository extends JpaRepository<UserDrivingState, Long> {

    Optional<UserDrivingState> findByUserId(Long userId);
}
