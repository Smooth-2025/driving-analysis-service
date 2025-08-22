package com.smooth.driving_analysis_service.progress.repository;

import com.smooth.driving_analysis_service.progress.entity.VUserProgress15Entity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VUserProgress15Repository extends JpaRepository<VUserProgress15Entity, String> {
    // 기본: findById(userId)
}
