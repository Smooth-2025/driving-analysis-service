package com.smooth.driving_analysis_service.progress.repository;

import com.smooth.driving_analysis_service.progress.domain.VUserProgress15;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VUserProgress15Repository extends JpaRepository<VUserProgress15, String> {
    // 기본: findById(userId)
}
