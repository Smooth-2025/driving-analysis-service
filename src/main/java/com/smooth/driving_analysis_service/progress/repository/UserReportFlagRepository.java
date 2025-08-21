package com.smooth.driving_analysis_service.progress.repository;

import com.smooth.driving_analysis_service.progress.domain.ReportFlag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserReportFlagRepository extends JpaRepository<ReportFlag, String> {
}
