package com.smooth.driving_analysis_service.reports.pipeline.service;

import com.smooth.driving_analysis_service.reports.pipeline.entity.DrivingAccumulatedStats;
import com.smooth.driving_analysis_service.reports.trigger.dto.DrivingSummaryV1;

/**
 * 주행 데이터 통합 서비스
 * XADD 스트림 데이터와 DrivingRecord를 통합하여 누적 통계를 생성
 */
public interface DrivingIntegrationService {
    
    /**
     * XADD 데이터와 DrivingRecord를 통합하여 누적 통계 저장
     * 
     * @param summary XADD 스트림에서 받은 주행 요약 데이터
     * @return 저장된 누적 통계 엔티티
     */
    DrivingAccumulatedStats integrateAndSave(DrivingSummaryV1 summary);
}