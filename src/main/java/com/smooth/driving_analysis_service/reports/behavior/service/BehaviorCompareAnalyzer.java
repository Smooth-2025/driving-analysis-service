package com.smooth.driving_analysis_service.reports.behavior.service;

import com.smooth.driving_analysis_service.reports.behavior.dto.response.CompareDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.TotalCountsDto;
import com.smooth.driving_analysis_service.reports.behavior.repository.BehaviorTotalCountsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 이전 리포트 대비 증감 분석을 담당하는 컴포넌트
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BehaviorCompareAnalyzer {

    private final BehaviorTotalCountsRepository totalCountsRepository;

    /**
     * 현재 리포트와 이전 리포트를 비교하여 증감 분석 수행
     */
    public CompareDto analyzeCompare(Long currentReportId, TotalCountsDto currentCounts) {
        try {
            // 이전 리포트 ID 계산 (현재 - 1)
            Long previousReportId = currentReportId - 1;
            
            // 이전 리포트 데이터 조회
            TotalCountsDto previousCounts = getPreviousReportCounts(previousReportId);
            
            // 증감 분석 수행
            CompareDto compareResult = calculateCompare(previousCounts, currentCounts);
            
            // 통합 코멘트 생성 추가
            String integratedComment = generateIntegratedComment(previousCounts, currentCounts);
            compareResult.setComment(integratedComment);
            
            return compareResult;
            
        } catch (Exception e) {
            log.error("증감 분석 실패 - currentReportId: {}", currentReportId, e);
            return createDefaultCompare(currentCounts);
        }
    }

    private TotalCountsDto getPreviousReportCounts(Long previousReportId) {
        try {
            TotalCountsDto previousCounts = totalCountsRepository.findTotalCountsByReportId(previousReportId);
            
            if (previousCounts == null) {
                log.info("이전 리포트 데이터 없음 - previousReportId: {}", previousReportId);
                return TotalCountsDto.of(0, 0, 0); // 기본값
            }
            
            return previousCounts;
            
        } catch (Exception e) {
            log.warn("이전 리포트 조회 실패 - previousReportId: {}", previousReportId, e);
            return TotalCountsDto.of(0, 0, 0); // 기본값
        }
    }

    private CompareDto calculateCompare(TotalCountsDto previous, TotalCountsDto current) {
        // 전체 증감률 계산
        double totalIncdec = calculateIncreaseRate(previous.getTotal(), current.getTotal());

        // 행동별 증감 데이터
        CompareDto.ChartDto chart = CompareDto.ChartDto.builder()
                .hardBrake(CompareDto.BeforeAfterDto.builder()
                        .before(previous.getHardBrake())
                        .current(current.getHardBrake())
                        .build())
                .rapidAccel(CompareDto.BeforeAfterDto.builder()
                        .before(previous.getRapidAccel())
                        .current(current.getRapidAccel())
                        .build())
                .laneChange(CompareDto.BeforeAfterDto.builder()
                        .before(previous.getLaneChange())
                        .current(current.getLaneChange())
                        .build())
                .build();

        return CompareDto.builder()
                .incdec(totalIncdec)
                .chart(chart)
                .build();
    }

    private double calculateIncreaseRate(Integer before, Integer current) {
        if (before == null || before == 0) {
            return current != null && current > 0 ? 100.0 : 0.0; // 이전이 0이면 100% 증가 또는 0%
        }
        
        if (current == null) {
            return -100.0; // 현재가 null이면 100% 감소
        }

        double rate = ((double) (current - before) / before) * 100;
        return Math.round(rate * 100.0) / 100.0; // 소수점 2자리 반올림
    }

    /**
     * 🅲 통합 코멘트 생성 (명세 요구사항)
     * 기준: 전체 위험 행동 총합의 증가/감소/유지
     * 괄호: 행동별 증감 기호(↑/↓/↔)
     */
    private String generateIntegratedComment(TotalCountsDto previous, TotalCountsDto current) {
        // 전체 증감 판단
        int totalDiff = current.getTotal() - previous.getTotal();
        String overallTrend;
        
        if (totalDiff > 0) {
            overallTrend = "전반적으로 증가했습니다";
        } else if (totalDiff < 0) {
            overallTrend = "전반적으로 감소했습니다";
        } else {
            overallTrend = "변화가 크지 않습니다";
        }
        
        // 행동별 증감 기호 생성
        String hardBrakeSymbol = getChangeSymbol(previous.getHardBrake(), current.getHardBrake());
        String rapidAccelSymbol = getChangeSymbol(previous.getRapidAccel(), current.getRapidAccel());
        String laneChangeSymbol = getChangeSymbol(previous.getLaneChange(), current.getLaneChange());
        
        return String.format("저번 리포트 대비 위험 운전 행동이 %s. (급가속 %s, 급제동 %s, 차선변경 %s)",
                overallTrend, rapidAccelSymbol, hardBrakeSymbol, laneChangeSymbol);
    }
    
    private String getChangeSymbol(Integer before, Integer current) {
        if (before == null) before = 0;
        if (current == null) current = 0;
        
        if (current > before) return "↑";
        if (current < before) return "↓";
        return "↔";
    }

    private CompareDto createDefaultCompare(TotalCountsDto currentCounts) {
        // 이전 데이터가 없을 때 기본 비교 데이터
        CompareDto.ChartDto chart = CompareDto.ChartDto.builder()
                .hardBrake(CompareDto.BeforeAfterDto.builder()
                        .before(0)
                        .current(currentCounts.getHardBrake())
                        .build())
                .rapidAccel(CompareDto.BeforeAfterDto.builder()
                        .before(0)
                        .current(currentCounts.getRapidAccel())
                        .build())
                .laneChange(CompareDto.BeforeAfterDto.builder()
                        .before(0)
                        .current(currentCounts.getLaneChange())
                        .build())
                .build();

        double totalIncdec = currentCounts.getTotal() > 0 ? 100.0 : 0.0;
        
        // 첫 리포트 기본 코멘트
        String defaultComment = "첫 번째 리포트로 이전 데이터와 비교할 수 없습니다.";

        return CompareDto.builder()
                .incdec(totalIncdec)
                .chart(chart)
                .comment(defaultComment)
                .build();
    }
}