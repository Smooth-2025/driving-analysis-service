package com.smooth.driving_analysis_service.reports.accident_reaction.repository;

import com.smooth.driving_analysis_service.reports.accident_reaction.entity.AlertRenderEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRenderEventRepository extends JpaRepository<AlertRenderEvent, String> {
    
    List<AlertRenderEvent> findByUserId(Long userId);
    
    List<AlertRenderEvent> findByDrivingId(String drivingId);
    
    List<AlertRenderEvent> findByDrivingIdIn(List<String> drivingIds);
}