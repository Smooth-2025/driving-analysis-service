package com.smooth.driving_analysis_service.batch.service;

import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;

import java.util.List;

public interface ReportFinderService {
    List<MilestoneReport> findReports(MilestoneReport.Status status);
    int countItems(Long reportId);
}
