package com.smooth.driving_analysis_service.driving.repository;

import com.smooth.driving_analysis_service.driving.entity.UserDrivingState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDrivingStateRepository extends JpaRepository<UserDrivingState, Long> {
}
