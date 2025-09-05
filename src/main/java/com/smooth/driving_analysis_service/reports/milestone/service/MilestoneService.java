package com.smooth.driving_analysis_service.reports.milestone.service;

import com.smooth.driving_analysis_service.reports.milestone.dto.response.MilestoneReportResponse;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;

import java.util.List;
import java.util.Optional;

public interface MilestoneService {
    void updateRead(long id, boolean read);
    List<MilestoneReportResponse> listByUser(long userId);
    MilestoneReportResponse getStamp(long id);
    
    /**
     * 주행 완료 시 마일스톤 처리
     */
    void processDrivingCompleted(Long userId, String drivingId);
    
    /**
     * 리포트 완료 처리 (배치에서 호출)
     */
    void markReportCompleted(Long reportId);
    
    /**
     * 사용자의 현재 활성 리포트 조회
     */
    Optional<MilestoneReport> getActiveReport(Long userId);
}
