package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import com.smooth.driving_analysis_service.reports.accident_reaction.repository.AccidentReactionMetricRepository;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneItem;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccidentReactionBatchService {
    
    private final AccidentReactionMetricRepository accidentReactionMetricRepository;
    private final MilestoneItemRepository milestoneItemRepository;

    @Transactional
    public void createOrUpdateInterimSnapshot(Long reportId) {
        log.info("[ACCIDENT_REACTION] Creating INTERIM snapshot for reportId: {}", reportId);
        
        try {
            // 리포트에 포함된 주행 ID들 조회
            List<MilestoneItem> items = milestoneItemRepository.findByReportIdOrderByOrderNoAsc(reportId);
            List<String> drivingIds = items.stream()
                    .map(MilestoneItem::getDrivingId)
                    .toList();
            
            if (drivingIds.isEmpty()) {
                log.warn("[ACCIDENT_REACTION] No driving IDs found for reportId: {}", reportId);
                return;
            }
            
            // 사고 대응 메트릭 조회 및 분석
            List<com.smooth.driving_analysis_service.reports.accident_reaction.entity.AccidentReactionMetric> metrics = 
                    accidentReactionMetricRepository.findByDrivingIdIn(drivingIds);
            
            // INTERIM 스냅샷 생성/갱신 로직
            // TODO: 실제 스냅샷 테이블에 저장하는 로직 구현
            
            log.info("[ACCIDENT_REACTION] INTERIM snapshot created for reportId: {}, metrics count: {}", 
                    reportId, metrics.size());
            
        } catch (Exception e) {
            log.error("[ACCIDENT_REACTION] Failed to create INTERIM snapshot for reportId: {}", reportId, e);
            throw e;
        }
    }

    @Transactional
    public void createFinalSnapshot(Long reportId) {
        log.info("[ACCIDENT_REACTION] Creating FINAL snapshot for reportId: {}", reportId);
        
        try {
            // 리포트에 포함된 주행 ID들 조회
            List<MilestoneItem> items = milestoneItemRepository.findByReportIdOrderByOrderNoAsc(reportId);
            List<String> drivingIds = items.stream()
                    .map(MilestoneItem::getDrivingId)
                    .toList();
            
            if (drivingIds.isEmpty()) {
                log.warn("[ACCIDENT_REACTION] No driving IDs found for reportId: {}", reportId);
                return;
            }
            
            // 사고 대응 메트릭 조회 및 분석
            List<com.smooth.driving_analysis_service.reports.accident_reaction.entity.AccidentReactionMetric> metrics = 
                    accidentReactionMetricRepository.findByDrivingIdIn(drivingIds);
            
            // FINAL 스냅샷 생성 로직
            // TODO: 실제 스냅샷 테이블에 저장하는 로직 구현
            
            log.info("[ACCIDENT_REACTION] FINAL snapshot created for reportId: {}, metrics count: {}", 
                    reportId, metrics.size());
            
        } catch (Exception e) {
            log.error("[ACCIDENT_REACTION] Failed to create FINAL snapshot for reportId: {}", reportId, e);
            throw e;
        }
    }
}