package com.smooth.driving_analysis_service.reports.basic_summary.service;

import com.smooth.driving_analysis_service.reports.basic_summary.dto.BasicSummaryResponse;
import com.smooth.driving_analysis_service.reports.basic_summary.entity.BasicSummary;
import com.smooth.driving_analysis_service.reports.basic_summary.repository.BasicSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BasicSummaryServiceImpl implements BasicSummaryService {

    private final BasicSummaryRepository basicSummaryRepository;

    @Override
    public void generateInterimReport(Long reportId, Long userId) {
        log.info("Generating interim basic summary: reportId={}", reportId);
        
        // 기존 INTERIM 스냅샷 조회 (있으면 갱신, 없으면 생성)
        BasicSummary summary = basicSummaryRepository
                .findByReportIdAndSnapshotType(reportId, BasicSummary.SnapshotType.INTERIM)
                .orElse(BasicSummary.builder()
                        .reportId(reportId)
                        .userId(userId)
                        .snapshotType(BasicSummary.SnapshotType.INTERIM)
                        .build());
        
        // 누적 통계 기반 계산
        updateSummaryData(summary, reportId);
        
        basicSummaryRepository.save(summary);
        log.info("Interim basic summary saved: reportId={}", reportId);
    }

    @Override
    public void generateFinalReport(Long reportId, Long userId) {
        log.info("Generating final basic summary: reportId={}", reportId);
        
        BasicSummary summary = BasicSummary.builder()
                .reportId(reportId)
                .userId(userId)
                .snapshotType(BasicSummary.SnapshotType.FINAL)
                .build();
        
        // 누적 통계 기반 계산
        updateSummaryData(summary, reportId);
        
        basicSummaryRepository.save(summary);
        log.info("Final basic summary saved: reportId={}", reportId);
    }

    @Override
    @Transactional(readOnly = true)
    public BasicSummaryResponse getBasicSummary(Long reportId) {
        // FINAL 우선, 없으면 INTERIM 조회
        BasicSummary summary = basicSummaryRepository
                .findByReportIdAndSnapshotType(reportId, BasicSummary.SnapshotType.FINAL)
                .orElse(basicSummaryRepository
                        .findByReportIdAndSnapshotType(reportId, BasicSummary.SnapshotType.INTERIM)
                        .orElse(null));
        
        if (summary == null) {
            log.warn("No basic summary found for reportId={}", reportId);
            return null;
        }
        
        return BasicSummaryResponse.builder()
                .reportId(String.valueOf(reportId))
                .totalDistanceKm(summary.getTotalDistanceKm())
                .periodStart(summary.getPeriodStart())
                .periodEnd(summary.getPeriodEnd())
                .averageDurationSec(summary.getAverageDurationSec())
                .averageDistanceKm(summary.getAverageDistanceKm())
                .averageSpeedKmh(summary.getAverageSpeedKmh())
                .averageCruiseRatio(summary.getAverageCruiseRatio())
                .build();
    }

    /**
     * driving_accumulated_stats 기반으로 요약 데이터 계산 및 업데이트
     */
    private void updateSummaryData(BasicSummary summary, Long reportId) {
        var projection = basicSummaryRepository.calculateSummaryByReportId(reportId);
        
        if (projection == null) {
            log.warn("No accumulated stats found for reportId={}", reportId);
            return;
        }
        
        // BigDecimal 변환 및 반올림
        summary.setTotalDistanceKm(toBigDecimal(projection.getTotalDistanceKm(), 2));
        summary.setAverageDurationSec(toBigDecimal(projection.getAverageDurationSec(), 2));
        summary.setAverageDistanceKm(toBigDecimal(projection.getAverageDistanceKm(), 2));
        summary.setAverageSpeedKmh(toBigDecimal(projection.getAverageSpeedKmh(), 1));
        summary.setAverageCruiseRatio(toBigDecimal(projection.getAverageCruiseRatio(), 3));
        
        // 날짜 파싱
        summary.setPeriodStart(parseDate(projection.getPeriodStart()));
        summary.setPeriodEnd(parseDate(projection.getPeriodEnd()));
        
        log.debug("Summary data updated: totalDistance={}, avgSpeed={}", 
                summary.getTotalDistanceKm(), summary.getAverageSpeedKmh());
    }

    private BigDecimal toBigDecimal(Double value, int scale) {
        if (value == null) return BigDecimal.ZERO;
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP);
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null) return null;
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            log.warn("Failed to parse date: {}", dateStr);
            return null;
        }
    }
}