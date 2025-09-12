package com.smooth.driving_analysis_service.reports.basic_summary.service;

import com.smooth.driving_analysis_service.reports.basic_summary.dto.response.BasicSummaryResponseDto;
import com.smooth.driving_analysis_service.reports.basic_summary.entity.BasicSummarySnapshot;
import com.smooth.driving_analysis_service.reports.basic_summary.repository.BasicSummarySnapshotRepository;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicSummaryServiceImpl implements BasicSummaryService {
    
    private final BasicSummarySnapshotRepository basicSummarySnapshotRepository;
    private final MilestoneReportRepository milestoneReportRepository;
    
    @Override
    public BasicSummaryResponseDto getBasicSummaryByReportId(String reportId, Long userId) {
        try {
            // 1. 스냅샷 우선 조회 (새로운 reportId 형식 지원)
            Optional<BasicSummarySnapshot> snapshot = basicSummarySnapshotRepository.findByReportId(reportId);
            if (snapshot.isPresent()) {
                log.info("Found snapshot for reportId: {}, type: {}", reportId, snapshot.get().getSnapshotType());
                return convertSnapshotToResponse(snapshot.get());
            }
            
            // 2. 스냅샷이 없으면 기존 로직 사용 (레거시 지원)
            log.info("No snapshot found, using legacy logic for reportId: {}", reportId);
            return getLegacyBasicSummary(reportId, userId);
            
        } catch (Exception e) {
            log.error("Error getting basic summary for reportId: {}", reportId, e);
            return createEmptyResponse(reportId);
        }
    }
    
    /**
     * 스냅샷을 응답 DTO로 변환
     */
    private BasicSummaryResponseDto convertSnapshotToResponse(BasicSummarySnapshot snapshot) {
        return BasicSummaryResponseDto.builder()
                .reportId(snapshot.getReportId())
                .totalDistanceKm(snapshot.getTotalDistanceKm() != null ? snapshot.getTotalDistanceKm() : 0.0)
                .totalDrivingTimeMinutes(snapshot.getTotalDrivingTimeMinutes() != null ? snapshot.getTotalDrivingTimeMinutes() : 0)
                .averageSpeedKmh(snapshot.getAverageSpeedKmh() != null ? snapshot.getAverageSpeedKmh() : 0.0)
                .maxSpeedKmh(snapshot.getMaxSpeedKmh() != null ? snapshot.getMaxSpeedKmh() : 0.0)
                .drivingCount(snapshot.getDrivingCount() != null ? snapshot.getDrivingCount() : 0)
                .build();
    }
    
    /**
     * 레거시 로직 (기존 reportId 형식 지원)
     */
    private BasicSummaryResponseDto getLegacyBasicSummary(String reportId, Long userId) {
        try {
            // 숫자 형식의 기존 reportId 처리
            if (reportId.matches("\\d+")) {
                Long reportIdLong = Long.parseLong(reportId);
                Optional<MilestoneReport> reportOpt = milestoneReportRepository.findById(reportIdLong);
                if (reportOpt.isPresent() && reportOpt.get().getUserId().equals(userId)) {
                    // TODO: DrivingAccumulatedStats를 이용한 실시간 계산
                    return createEmptyResponse(reportId);
                }
            }
            
            // 새로운 형식이지만 스냅샷이 없는 경우
            log.warn("No data found for reportId: {}", reportId);
            return createEmptyResponse(reportId);
            
        } catch (Exception e) {
            log.error("Error in legacy basic summary for reportId: {}", reportId, e);
            return createEmptyResponse(reportId);
        }
    }
    
    /**
     * 빈 응답 생성 (데이터가 없을 때)
     */
    private BasicSummaryResponseDto createEmptyResponse(String reportId) {
        return BasicSummaryResponseDto.builder()
                .reportId(reportId.matches("\\d+") ? Long.parseLong(reportId) : null)
                .totalDistanceKm(0.0)
                .totalDrivingTimeMinutes(0)
                .averageSpeedKmh(0.0)
                .maxSpeedKmh(0.0)
                .drivingCount(0)
                .build();
    }
}