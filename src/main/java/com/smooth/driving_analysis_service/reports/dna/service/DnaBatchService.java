package com.smooth.driving_analysis_service.reports.dna.service;

import com.smooth.driving_analysis_service.reports.dna.entity.DnaSnapshot;

public interface DnaBatchService {
    /**
     * 중간 분석 실행 (4/8/12회 도달시)
     * @param reportId 리포트 ID
     * @return 생성/갱신된 DnaSnapshot
     */
    DnaSnapshot runInterim(Long reportId);

    /**
     * 최종 분석 실행 (15회 도달시)
     * @param reportId 리포트 ID
     * @return 생성/갱신된 DnaSnapshot
     */
    DnaSnapshot runFinal(Long reportId);
}