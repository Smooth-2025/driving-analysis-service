package com.smooth.driving_analysis_service.driving.service;

import com.smooth.driving_analysis_service.driving.dto.result.DrivingAnalysisResultDto;
import com.smooth.driving_analysis_service.driving.dto.result.EventAnalysisResultDto;


import java.util.List;
import java.util.Map;

public interface AthenaQueryService {

    DrivingAnalysisResultDto getDrivingAnalysis(String drivingId);

    EventAnalysisResultDto getEventAnalysis(String drivingId);
    
    /**
     * 일반적인 Athena 쿼리 실행
     * @param query SQL 쿼리
     * @return 쿼리 결과 (Map 리스트)
     */
    List<Map<String, Object>> executeQuery(String query);
}
