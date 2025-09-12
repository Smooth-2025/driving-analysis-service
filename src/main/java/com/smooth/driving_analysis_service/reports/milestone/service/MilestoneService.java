package com.smooth.driving_analysis_service.reports.milestone.service;

import com.smooth.driving_analysis_service.reports.milestone.dto.response.MilestoneReportResponseDto;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;

import java.util.List;

public interface MilestoneService {
    
    void updateRead(long id, boolean read);
    MilestoneReport updateReadByReportId(String reportId, boolean read);
    List<MilestoneReportResponseDto> listByUser(long userId);
    MilestoneReportResponseDto getStamp(long id);
    MilestoneReportResponseDto getStampByUserId(long userId);
    
    /**
     * 주행 완료 처리 - 마일스톤 아이템 추가 및 카운트 증가
     */
    void processDrivingCompleted(Long userId, String drivingId);
    
    /**
     * 리포트 완료 처리 (15회 도달 시)
     */
    void markReportCompleted(Long reportId);
}