package com.smooth.driving_analysis_service.reports.batch.service;

import com.smooth.driving_analysis_service.driving.repository.DrivingRecordRepository;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneItem;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneItemRepository;
import com.smooth.driving_analysis_service.reports.dna.service.DnaBatchService;
import com.smooth.driving_analysis_service.reports.basic_summary.service.BasicSummaryBatchService;
import com.smooth.driving_analysis_service.reports.behavior.service.BehaviorBatchService;
import com.smooth.driving_analysis_service.reports.accident_reaction.service.AccidentReactionBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CycleReportBatchServiceImpl implements CycleReportBatchService {
    
    private final MilestoneReportRepository milestoneReportRepository;
    private final MilestoneItemRepository milestoneItemRepository;
    private final DrivingRecordRepository drivingRecordRepository;
    
    // 배치 서비스들
    private final DnaBatchService dnaBatchService;
    private final BasicSummaryBatchService basicSummaryBatchService;
    private final BehaviorBatchService behaviorBatchService;
    private final AccidentReactionBatchService accidentReactionBatchService;
    
    @Override
    public void processAllUsers(String dateStr) {
        log.info("Processing cycle reports for all users on date: {}", dateStr);
        
        // COLLECTING 상태인 모든 리포트 조회
        List<MilestoneReport> collectingReports = milestoneReportRepository
                .findAllByStatus(MilestoneReport.Status.COLLECTING);
        
        log.info("Found {} collecting reports", collectingReports.size());
        
        for (MilestoneReport report : collectingReports) {
            try {
                processUser(report.getUserId(), dateStr);
            } catch (Exception e) {
                log.error("Failed to process user: {}", report.getUserId(), e);
            }
        }
        
        // PROCESSING 상태인 리포트들도 FINAL 처리
        List<MilestoneReport> processingReports = milestoneReportRepository
                .findAllByStatus(MilestoneReport.Status.PROCESSING);
        
        log.info("Found {} processing reports for FINAL", processingReports.size());
        
        for (MilestoneReport report : processingReports) {
            try {
                processFinalReport(report, dateStr);
            } catch (Exception e) {
                log.error("Failed to process FINAL for reportId: {}", report.getId(), e);
            }
        }
    }
    
    @Override
    @Transactional
    public void processUser(Long userId, String dateStr) {
        log.info("Processing cycle report for userId: {}", userId);
        
        // 현재 활성 리포트 조회
        var activeReportOpt = milestoneReportRepository
                .findFirstByUserIdAndStatusOrderByIdDesc(userId, MilestoneReport.Status.COLLECTING);
        
        if (activeReportOpt.isEmpty()) {
            log.debug("No active report for userId: {}", userId);
            return;
        }
        
        MilestoneReport activeReport = activeReportOpt.get();
        int numberOfDriving = activeReport.getNumberOfDriving();
        
        log.info("User {} has {} trips in current cycle", userId, numberOfDriving);
        
        // 분기 로직
        if (numberOfDriving == 0) {
            log.debug("Skipping user {} - no trips", userId);
            return;
        }
        
        if (numberOfDriving >= 1 && numberOfDriving < 15) {
            // INTERIM 생성/업서트
            processInterimReport(activeReport, dateStr);
        }
        
        // PROCESSING 상태는 별도로 처리됨
    }
    
    /**
     * INTERIM 리포트 처리 (1~14회)
     */
    private void processInterimReport(MilestoneReport report, String dateStr) {
        Long userId = report.getUserId();
        int cycleNo = report.getCycleNo();
        String interimReportId = String.format("u%d_c%d_interim", userId, cycleNo);
        
        log.info("Processing INTERIM report: {} for {} trips", interimReportId, report.getNumberOfDriving());
        
        // 현재 사이클의 drivingId 목록 조회
        List<String> drivingIds = getCurrentCycleDrivingIds(report.getId());
        
        if (drivingIds.isEmpty()) {
            log.warn("No driving IDs found for reportId: {}", report.getId());
            return;
        }
        
        // 4개 분석 모듈에 INTERIM 처리 요청
        try {
            dnaBatchService.materializeByDrivingIds(interimReportId, userId, drivingIds);
            basicSummaryBatchService.materializeByDrivingIds(interimReportId, userId, drivingIds);
            behaviorBatchService.materializeByDrivingIds(interimReportId, userId, drivingIds);
            accidentReactionBatchService.materializeByDrivingIds(interimReportId, userId, drivingIds);
            
            log.info("INTERIM report processed successfully: {}", interimReportId);
        } catch (Exception e) {
            log.error("Failed to process INTERIM report: {}", interimReportId, e);
            throw e;
        }
    }
    
    /**
     * FINAL 리포트 처리 (15회)
     */
    private void processFinalReport(MilestoneReport report, String dateStr) {
        Long userId = report.getUserId();
        int cycleNo = report.getCycleNo();
        String finalReportId = String.format("u%d_c%d_final_%s", userId, cycleNo, dateStr);
        
        log.info("Processing FINAL report: {} for {} trips", finalReportId, report.getNumberOfDriving());
        
        // 현재 사이클의 drivingId 목록 조회 (정확히 15개)
        List<String> drivingIds = getCurrentCycleDrivingIds(report.getId());
        
        if (drivingIds.size() != 15) {
            log.error("Expected 15 driving IDs but got {} for reportId: {}", drivingIds.size(), report.getId());
            return;
        }
        
        // 4개 분석 모듈에 FINAL 처리 요청
        try {
            dnaBatchService.materializeByDrivingIds(finalReportId, userId, drivingIds);
            basicSummaryBatchService.materializeByDrivingIds(finalReportId, userId, drivingIds);
            behaviorBatchService.materializeByDrivingIds(finalReportId, userId, drivingIds);
            accidentReactionBatchService.materializeByDrivingIds(finalReportId, userId, drivingIds);
            
            // FINAL 마일스톤 리포트 생성
            MilestoneReport finalReport = MilestoneReport.newFinal(userId, cycleNo, dateStr);
            milestoneReportRepository.save(finalReport);
            
            // 마일스톤 아이템들 복사 (FINAL용)
            List<MilestoneItem> items = milestoneItemRepository.findByReportIdOrderByOrderNo(report.getId());
            for (MilestoneItem item : items) {
                MilestoneItem finalItem = MilestoneItem.of(finalReport, item.getDrivingId(), item.getOrderNo());
                milestoneItemRepository.save(finalItem);
            }
            
            // 기존 리포트 완료 처리 및 numberOfDriving 리셋
            report.setStatus(MilestoneReport.Status.COMPLETED);
            report.setNumberOfDriving(0); // 사이클 리셋
            milestoneReportRepository.save(report);
            
            log.info("FINAL report processed successfully: {}", finalReportId);
            
        } catch (Exception e) {
            log.error("Failed to process FINAL report: {}", finalReportId, e);
            throw e;
        }
    }
    
    /**
     * 현재 사이클의 drivingId 목록 조회
     */
    private List<String> getCurrentCycleDrivingIds(Long reportId) {
        return milestoneItemRepository.findDrivingIdsByReportId(reportId);
    }
}