package com.smooth.driving_analysis_service.reports.dna.service;

import com.smooth.driving_analysis_service.reports.dna.entity.DnaSnapshot;

import java.util.List;

public interface DnaBatchService {
    /**
     * 중간 분석 실행 (4/8/12회 도달시) - 레거시 방식 (호환성 유지)
     * @param reportId 리포트 ID
     * @return 생성/갱신된 DnaSnapshot
     */
    DnaSnapshot runInterim(Long reportId);

    /**
     * 최종 분석 실행 (15회 도달시) - 레거시 방식 (호환성 유지)
     * @param reportId 리포트 ID
     * @return 생성/갱신된 DnaSnapshot
     */
    DnaSnapshot runFinal(Long reportId);
    
    /**
     * 사이클 기반 분석 (새로운 시스템)
     * @param reportId 리포트 ID (INTERIM: u{userId}_c{cycleNo}_interim, FINAL: u{userId}_c{cycleNo}_final_{yyyyMMdd})
     * @param userId 사용자 ID
     * @param drivingIds 분석 대상 주행 ID 목록 (1~15개)
     */
    void materializeByDrivingIds(String reportId, Long userId, List<String> drivingIds);
}