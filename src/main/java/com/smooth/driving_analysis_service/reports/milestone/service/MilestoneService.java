package com.smooth.driving_analysis_service.reports.milestone.service;

import com.smooth.driving_analysis_service.reports.milestone.dto.response.MilestoneReportResponse;

import java.util.List;

public interface MilestoneService {
    void updateRead(long id, boolean read);
    List<MilestoneReportResponse> listByUser(long userId);
    MilestoneReportResponse getStamp(long id);
}
