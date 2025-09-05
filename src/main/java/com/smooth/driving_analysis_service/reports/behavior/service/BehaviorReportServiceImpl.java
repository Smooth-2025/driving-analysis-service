package com.smooth.driving_analysis_service.reports.behavior.service;

import com.smooth.driving_analysis_service.reports.behavior.dto.response.BehaviorAnalysisResponseDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.TotalCountsDto;
import com.smooth.driving_analysis_service.reports.behavior.repository.BehaviorTotalCountsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BehaviorReportServiceImpl implements BehaviorReportService {

    private final BehaviorTotalCountsRepository totalCountsRepository;

    @Override
    public BehaviorAnalysisResponseDto getBehaviorAnalysis(String reportId) {
        log.info("Task 1: totalCounts 조회 - reportId: {}", reportId);

        try {
            // reportId에서 숫자 부분 추출 (예: "u1_r3_20250901" -> 3)
            Long reportIdLong = extractReportIdFromString(reportId);

            // Task 1: totalCounts 조회
            TotalCountsDto totalCounts = totalCountsRepository.findTotalCountsByReportId(reportIdLong);
            
            if (totalCounts == null) {
                log.warn("리포트 데이터 없음 - reportId: {}", reportId);
                totalCounts = TotalCountsDto.of(0, 0, 0);
            }

            // Task 1에서는 totalCounts만 구현
            return BehaviorAnalysisResponseDto.builder()
                    .reportId(reportId)
                    .totalCounts(BehaviorAnalysisResponseDto.TotalCounts.builder()
                            .hardBrake(totalCounts.getHardBrake())
                            .rapidAccel(totalCounts.getRapidAccel())
                            .laneChange(totalCounts.getLaneChange())
                            .total(totalCounts.getTotal())
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("위험운전 행동 분석 조회 실패 - reportId: {}", reportId, e);
            // 오류 시 기본 데이터 반환
            return BehaviorAnalysisResponseDto.builder()
                    .reportId(reportId)
                    .totalCounts(BehaviorAnalysisResponseDto.TotalCounts.builder()
                            .hardBrake(0).rapidAccel(0).laneChange(0).total(0)
                            .build())
                    .build();
        }
    }

    // === 유틸 메서드 ===
    
    private Long extractReportIdFromString(String reportId) {
        try {
            // "u1_r3_20250901" 형식에서 "r3" 부분의 숫자 추출
            String[] parts = reportId.split("_");
            for (String part : parts) {
                if (part.startsWith("r")) {
                    return Long.parseLong(part.substring(1));
                }
            }
            return 1L; // 기본값
        } catch (Exception e) {
            log.warn("reportId 파싱 실패, 기본값 사용: {}", reportId, e);
            return 1L;
        }
    }
}