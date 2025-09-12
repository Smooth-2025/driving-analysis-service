package com.smooth.driving_analysis_service.reports.dna.service.impl;


import com.smooth.driving_analysis_service.reports.batch.dto.SnapshotType;
import com.smooth.driving_analysis_service.reports.dna.entity.DnaSnapshot;
import com.smooth.driving_analysis_service.reports.dna.repository.DnaSnapshotRepository;
import com.smooth.driving_analysis_service.reports.dna.service.DnaBatchService;
import com.smooth.driving_analysis_service.reports.dna.service.DnaComputeService;
import com.smooth.driving_analysis_service.reports.dna.service.DnaMetricSource;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneItemRepository;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DnaBatchServiceImpl implements DnaBatchService {

    private final MilestoneReportRepository reportRepo;
    private final MilestoneItemRepository itemRepo;
    private final DnaSnapshotRepository snapshotRepo;
    private final DnaComputeService compute;
    private final DnaMetricSource metricSource;

    @Override
    @Transactional
    public DnaSnapshot runInterim(Long reportId) {
        log.debug("DNA runInterim called with reportId: {}", reportId);
        
        MilestoneReport report = reportRepo.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("report not found: " + reportId));

        log.debug("Found report: id={}, reportId={}, status={}, numberOfDriving={}", 
                report.getId(), report.getReportId(), report.getStatus(), report.getNumberOfDriving());

        if (report.getStatus() != MilestoneReport.Status.COLLECTING) {
            log.warn("Interim only for COLLECTING status, current: {}", report.getStatus());
            throw new IllegalStateException("Interim only for COLLECTING status, current: " + report.getStatus());
        }

        // 4/8/12회 도달 시점에서만 실행
        int numberOfDriving = report.getNumberOfDriving();
        int targetInterimCount;
        
        if (numberOfDriving >= 12)
            targetInterimCount = 12;
        else if (numberOfDriving >= 8)
            targetInterimCount = 8;
        else if (numberOfDriving >= 4)
            targetInterimCount = 4;
        else
            return null; // 4회 미만이면 실행하지 않음

        DnaSnapshot existing = snapshotRepo.findByReportId(reportId).orElse(null);
        if (existing != null && existing.getLastInterimCount() != null &&
                existing.getLastInterimCount() >= targetInterimCount) {
            return existing; // 이미 처리됨
        }

        return upsert(reportId, DnaSnapshot.Status.INTERIM, targetInterimCount);
    }

    @Override
    @Transactional
    public DnaSnapshot runFinal(Long reportId) {
        MilestoneReport report = reportRepo.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("report not found: " + reportId));

        if (report.getStatus() != MilestoneReport.Status.PROCESSING) {
            log.warn("Final only for PROCESSING status, current: {}", report.getStatus());
            throw new IllegalStateException("Final only for PROCESSING status, current: " + report.getStatus());
        }

        if (report.getNumberOfDriving() < 15) {
            throw new IllegalStateException("Final requires 15+ trips, current: " + report.getNumberOfDriving());
        }

        DnaSnapshot result = upsert(reportId, DnaSnapshot.Status.FINAL, null);

        report.setStatus(MilestoneReport.Status.COMPLETED);
        reportRepo.save(report);

        return result;
    }


    
    @Override
    @Transactional
    public void materializeByDrivingIds(String reportId, Long userId, List<String> drivingIds) {
        log.info("Materializing DNA snapshot: reportId={}, userId={}, drivingCount={}", 
                reportId, userId, drivingIds.size());
        
        try {
            // DNA 분석 실행
            var metrics = metricSource.getMetricsByDrivingIds(drivingIds);
            
            double A = metrics.getOrDefault("safe_driving_score", 0.0);
            double B = metrics.getOrDefault("eco_driving_score", 0.0);
            double C = metrics.getOrDefault("defensive_driving_score", 0.0);
            double D = metrics.getOrDefault("smooth_driving_score", 0.0);

            String code = compute.code(A, B, C, D);
            String headline = compute.headline(A, B, C, D);

            // INTERIM인지 FINAL인지 구분
            boolean isInterim = reportId.contains("_interim");
            DnaSnapshot.Status status = isInterim ? DnaSnapshot.Status.INTERIM : DnaSnapshot.Status.FINAL;

            DnaSnapshot snap;
            if (isInterim) {
                // INTERIM: 업서트 (덮어쓰기)
                snap = snapshotRepo.findByReportId(reportId).orElse(
                        DnaSnapshot.builder().reportId(reportId).userId(userId).build());
            } else {
                // FINAL: 신규 생성 (불변)
                snap = DnaSnapshot.builder().reportId(reportId).userId(userId).build();
            }

            snap.setStatus(status);
            snap.setCode(code);
            snap.setSafeDrivingScore(A);
            snap.setEcoDrivingScore(B);
            snap.setDefensiveDrivingScore(C);
            snap.setSmoothDrivingScore(D);
            snap.setHeadline(headline);

            if (isInterim) {
                snap.setLastInterimCount(drivingIds.size());
                snap.setLastInterimAt(LocalDateTime.now());
            }

            snapshotRepo.save(snap);
            
            log.info("DNA snapshot materialized successfully: {}", reportId);
            
        } catch (Exception e) {
            log.error("Failed to materialize DNA snapshot: {}", reportId, e);
            throw e;
        }
    }

    private DnaSnapshot upsert(Long reportId, DnaSnapshot.Status status, Integer interimCount) {
        MilestoneReport report = reportRepo.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("report not found: " + reportId));
        Long userId = report.getUserId();

        // DNA 분석 실행
        var metrics = metricSource.getMetrics(reportId);
        
        double A = metrics.getOrDefault("safe_driving_score", 0.0);
        double B = metrics.getOrDefault("eco_driving_score", 0.0);
        double C = metrics.getOrDefault("defensive_driving_score", 0.0);
        double D = metrics.getOrDefault("smooth_driving_score", 0.0);

        String code = compute.code(A, B, C, D);
        String headline = compute.headline(A, B, C, D);

        // 레거시 방식: Long reportId로 조회
        DnaSnapshot snap = snapshotRepo.findByReportId(reportId).orElse(
                DnaSnapshot.builder().reportId(reportId.toString()).userId(userId).build());

        snap.setStatus(status);
        snap.setCode(code);
        snap.setSafeDrivingScore(A);
        snap.setEcoDrivingScore(B);
        snap.setDefensiveDrivingScore(C);
        snap.setSmoothDrivingScore(D);
        snap.setHeadline(headline);

        if (status == DnaSnapshot.Status.INTERIM && interimCount != null) {
            snap.setLastInterimCount(interimCount);
            snap.setLastInterimAt(LocalDateTime.now());
        }

        return snapshotRepo.save(snap);
    }
}