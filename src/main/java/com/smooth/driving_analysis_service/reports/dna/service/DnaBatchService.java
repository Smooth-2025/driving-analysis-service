package com.smooth.driving_analysis_service.reports.dna.service;

import com.smooth.driving_analysis_service.reports.dna.entity.DnaSnapshot;

public interface DnaBatchService {
    
    /**
     * 중간 분석 실행 (4/8/12회)
     * @param reportId 리포트 ID
     * @return DNA 스냅샷
     */
    DnaSnapshot runInterim(Long reportId);
    
    /**
     * 최종 분석 실행 (15회)
     * @param reportId 리포트 ID
     * @return DNA 스냅샷
     */
    DnaSnapshot runFinal(Long reportId);
}
