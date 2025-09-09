package com.smooth.driving_analysis_service.reports.basic_summary.service;

import com.smooth.driving_analysis_service.reports.basic_summary.dto.BasicSummaryResponseDto;
import com.smooth.driving_analysis_service.reports.basic_summary.entity.BasicSummary;
import com.smooth.driving_analysis_service.reports.basic_summary.repository.BasicSummaryRepository;
import com.smooth.driving_analysis_service.pipeline.repository.DrivingAccumulatedStatsRepository;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicSummaryServiceImpl implements BasicSummaryService {
    
    private final BasicSummaryRepository basicSummaryRepository;
    private final DrivingAccumulatedStatsRepository drivingAccumulatedStatsRepository;
    private final MilestoneReportRepository milestoneReportRepository;
    
    @Override
    @Transactional(readOnly = true)
    public BasicSummaryResponseDto getBasicSummary(String reportId) {
        try {
            return getBasicSummary(Long.parseLong(reportId));
        } catch (NumberFormatException e) {
            throw new RuntimeException("잘못된 reportId 형식입니다: " + reportId);
        }
    }
    
    @Transactional(readOnly = true)
    public BasicSummaryResponseDto getBasicSummary(Long reportId) {
        log.info("기본 통계 조회 시작 - reportId: {}", reportId);
        
        try {
            // FINAL 스냅샷 우선 조회
            BasicSummary basicSummary = basicSummaryRepository.findFinalByReportId(reportId)
                    .orElseGet(() -> basicSummaryRepository.findInterimByReportId(reportId)
                            .orElse(null));
            
            if (basicSummary == null) {
                log.warn("기본 통계를 찾을 수 없습니다. reportId: {}", reportId);
                
                // 마일스톤 리포트가 존재하는지 확인
                MilestoneReport milestoneReport = milestoneReportRepository.findById(reportId).orElse(null);
                if (milestoneReport == null) {
                    throw new RuntimeException("해당 리포트가 존재하지 않습니다. reportId: " + reportId);
                }
                
                // 마일스톤 리포트는 있지만 기본 통계가 없는 경우 - 기본값 반환
                log.warn("마일스톤 리포트는 존재하지만 기본 통계 스냅샷이 없습니다. 기본값을 반환합니다. reportId: {}", reportId);
                return createDefaultBasicSummaryResponse(milestoneReport.getUserId(), reportId);
            }
            
            String reportIdStr = generateReportIdString(basicSummary.getUserId(), reportId);
            
            return BasicSummaryResponseDto.builder()
                    .reportId(reportIdStr)
                    .totalDistanceKm(toDouble(basicSummary.getTotalDistanceKm()))
                    .periodStart(basicSummary.getPeriodStart())
                    .periodEnd(basicSummary.getPeriodEnd())
                    .averageDurationSec(toDouble(basicSummary.getAverageDurationSec()))
                    .averageDistanceKm(toDouble(basicSummary.getAverageDistanceKm()))
                    .averageSpeedKmh(toDouble(basicSummary.getAverageSpeedKmh()))
                    .averageCruiseRatio(toDouble(basicSummary.getAverageCruiseRatio()))
                    .build();
        } catch (Exception e) {
            log.error("기본 통계 조회 중 오류 발생 - reportId: {}, error: {}", reportId, e.getMessage());
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void generateInterimReport(Long reportId, Long userId) {
        log.info("INTERIM 스냅샷 생성/갱신 시작 - reportId: {}", reportId);
        
        // 기존 INTERIM 스냅샷 삭제
        basicSummaryRepository.deleteByReportIdAndSnapshotType(reportId, BasicSummary.SnapshotType.INTERIM);
        
        // 새 INTERIM 스냅샷 생성
        createSnapshot(reportId, BasicSummary.SnapshotType.INTERIM);
        
        log.info("INTERIM 스냅샷 생성/갱신 완료 - reportId: {}", reportId);
    }
    
    @Override
    @Transactional
    public void generateFinalReport(Long reportId, Long userId) {
        log.info("FINAL 스냅샷 생성 시작 - reportId: {}", reportId);
        
        createSnapshot(reportId, BasicSummary.SnapshotType.FINAL);
        
        log.info("FINAL 스냅샷 생성 완료 - reportId: {}", reportId);
    }
    
    private void createSnapshot(Long reportId, BasicSummary.SnapshotType snapshotType) {
        // 마일스톤 리포트 조회
        MilestoneReport milestoneReport = milestoneReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("마일스톤 리포트를 찾을 수 없습니다. reportId: " + reportId));
        
        // 누적 통계 조회
        DrivingAccumulatedStatsRepository.BasicSummaryProjection projection = 
                drivingAccumulatedStatsRepository.getBasicSummaryByReportId(reportId);
        
        if (projection == null) {
            throw new RuntimeException("누적 통계 데이터를 찾을 수 없습니다. reportId: " + reportId);
        }
        
        // 스냅샷 생성
        BasicSummary basicSummary = BasicSummary.builder()
                .reportId(reportId)
                .userId(milestoneReport.getUserId())
                .totalDistanceKm(toBigDecimal(projection.getTotalDistanceKm()))
                .periodStart(projection.getPeriodStart())
                .periodEnd(projection.getPeriodEnd())
                .averageDurationSec(toBigDecimal(projection.getAverageDurationSec()))
                .averageDistanceKm(toBigDecimal(projection.getAverageDistanceKm()))
                .averageSpeedKmh(toBigDecimal(projection.getAverageSpeedKmh()))
                .averageCruiseRatio(toBigDecimal(projection.getAverageCruiseRatio()))
                .snapshotType(snapshotType)
                .build();
        
        basicSummaryRepository.save(basicSummary);
        
        log.info("{} 스냅샷 저장 완료 - reportId: {}, userId: {}", 
                snapshotType, reportId, milestoneReport.getUserId());
    }
    
    private String generateReportIdString(Long userId, Long reportId) {
        // u{userId}_r{reportId}_yyyyMMdd 형식으로 생성
        return String.format("u%d_r%d_%s", userId, reportId, 
                java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")));
    }
    
    private java.math.BigDecimal toBigDecimal(Double value) {
        if (value == null) {
            return java.math.BigDecimal.ZERO;
        }
        return java.math.BigDecimal.valueOf(value);
    }
    
    private Double toDouble(java.math.BigDecimal value) {
        if (value == null) {
            return 0.0;
        }
        return value.doubleValue();
    }
    
    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void createOrUpdateInterimSnapshot(Long reportId) {
        log.info("INTERIM 스냅샷 생성/갱신 시작 - reportId: {}", reportId);
        
        // 기존 INTERIM 스냅샷 삭제
        basicSummaryRepository.deleteByReportIdAndSnapshotType(reportId, BasicSummary.SnapshotType.INTERIM);
        
        // 새 INTERIM 스냅샷 생성
        createSnapshot(reportId, BasicSummary.SnapshotType.INTERIM);
        
        log.info("INTERIM 스냅샷 생성/갱신 완료 - reportId: {}", reportId);
    }
    
    @Override
    @Transactional
    public void createFinalSnapshot(Long reportId) {
        log.info("FINAL 스냅샷 생성 시작 - reportId: {}", reportId);
        
        createSnapshot(reportId, BasicSummary.SnapshotType.FINAL);
        
        log.info("FINAL 스냅샷 생성 완료 - reportId: {}", reportId);
    }
    
    /**
     * 스냅샷이 없을 때 기본값 응답 생성
     */
    private BasicSummaryResponseDto createDefaultBasicSummaryResponse(Long userId, Long reportId) {
        String reportIdStr = generateReportIdString(userId, reportId);
        LocalDate now = LocalDate.now();
        
        return BasicSummaryResponseDto.builder()
                .reportId(reportIdStr)
                .totalDistanceKm(0.0)
                .periodStart(now.minusDays(14)) // 기본 2주 기간
                .periodEnd(now)
                .averageDurationSec(0.0)
                .averageDistanceKm(0.0)
                .averageSpeedKmh(0.0)
                .averageCruiseRatio(0.0)
                .build();
    }
}