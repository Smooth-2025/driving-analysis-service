package com.smooth.driving_analysis_service.reports.accident_reaction.repository;

import com.smooth.driving_analysis_service.reports.accident_reaction.entity.AlertRenderEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlertRenderEventRepository extends JpaRepository<AlertRenderEvent, Long> {
    Optional<AlertRenderEvent> findByAlertId(String alertId);
}
