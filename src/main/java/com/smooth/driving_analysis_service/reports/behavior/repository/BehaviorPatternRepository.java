package com.smooth.driving_analysis_service.reports.behavior.repository;

import com.smooth.driving_analysis_service.reports.behavior.dto.projection.EventPatternProjectionDto;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * S3 event_data를 Athena로 쿼리하여 위험행동 패턴을 분석하는 Repository
 */
@Repository
public interface BehaviorPatternRepository {
    
    /**
     * 특정 리포트의 drivingId들에 대해 요일별, 시간대별, 행동별 이벤트 패턴을 조회
     * 
     * @param reportId 리포트 ID
     * @return 요일별, 시간대별, 행동별 집계 데이터
     */
    List<EventPatternProjectionDto> findEventPatternsByReportId(Long reportId);
    
    /**
     * 특정 drivingId들에 대해 요일별, 시간대별, 행동별 이벤트 패턴을 조회
     * 
     * @param drivingIds 주행 ID 목록
     * @return 요일별, 시간대별, 행동별 집계 데이터
     */
    List<EventPatternProjectionDto> findEventPatternsByDrivingIds(List<String> drivingIds);
}