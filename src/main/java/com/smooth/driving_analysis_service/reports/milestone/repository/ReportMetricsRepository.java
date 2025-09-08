package com.smooth.driving_analysis_service.reports.milestone.repository;

import com.smooth.driving_analysis_service.reports.milestone.entity.ReportMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportMetricsRepository extends JpaRepository<ReportMetrics, Long> {}
