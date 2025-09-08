package com.smooth.driving_analysis_service.reports.dna.service.impl;

import com.smooth.driving_analysis_service.driving.entity.DrivingRecord;
import com.smooth.driving_analysis_service.driving.repository.DrivingRecordRepository;
import com.smooth.driving_analysis_service.reports.accident_reaction.entity.AccidentReactionMetric;
import com.smooth.driving_analysis_service.reports.accident_reaction.repository.AccidentReactionMetricRepository;
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
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class DnaBatchServiceImpl implements DnaBatchService {

    private final MilestoneReportRepository reportRepo;
    private final MilestoneItemRepository itemRepo;
    private final DrivingRecordRepository drivingRepo;
    private final AccidentReactionMetricRepository reactionRepo;
    private final DnaSnapshotRepository snapshotRepo;
    private final DnaComputeService compute;
    private final DnaMetricSource metricSource;

    @Override
    @Transactional
    public DnaSnapshot runInterim(Long reportId) {
        MilestoneReport report = reportRepo.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("report not found: " + reportId));

        if (report.getStatus() != MilestoneReport.Status.COLLECTING) {
            throw new IllegalStateException("Interim only for COLLECTING status, current: " + report.getStatus());
        }

        int currentCount = report.getNumberOfDriving();

        if (currentCount < 4) {
            throw new IllegalStateException("Interim requires at least 4 driving records, current: " + currentCount);
        }

        int targetInterimCount = (currentCount / 4) * 4;
        if (targetInterimCount > 12)
            targetInterimCount = 12;

        DnaSnapshot existing = snapshotRepo.findByReportId(reportId).orElse(null);
        if (existing != null && existing.getLastInterimCount() != null &&
                existing.getLastInterimCount() >= targetInterimCount) {
            return existing;
        }

        return upsert(reportId, DnaSnapshot.Status.INTERIM, targetInterimCount);
    }

    @Override
    @Transactional
    public DnaSnapshot runFinal(Long reportId) {
        MilestoneReport report = reportRepo.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("report not found: " + reportId));

        if (report.getStatus() != MilestoneReport.Status.PROCESSING) {
            throw new IllegalStateException("Final only for PROCESSING status, current: " + report.getStatus());
        }

        if (report.getNumberOfDriving() != 15) {
            throw new IllegalStateException(
                    "Final requires exactly 15 driving records, current: " + report.getNumberOfDriving());
        }

        DnaSnapshot result = upsert(reportId, DnaSnapshot.Status.FINAL, null);

        report.setStatus(MilestoneReport.Status.COMPLETED);
        reportRepo.save(report);

        return result;
    }

    private DnaSnapshot upsert(Long reportId, DnaSnapshot.Status status, Integer interimCount) {
        MilestoneReport report = reportRepo.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("report not found: " + reportId));
        Long userId = report.getUserId();

        var items = itemRepo.findByReportIdOrderByOrderNoAsc(report.getId());
        List<String> drivingIds = items.stream().map(i -> i.getDrivingId()).toList();
        if (drivingIds.isEmpty())
            throw new IllegalStateException("no drivingIds for report " + reportId);

        var input = metricSource.loadForReport(reportId, drivingIds);
        var drivings = input.drivings();

        double totalKmFromSource = drivings.stream()
                .mapToDouble(d -> nz(d.distanceKm()))
                .sum();

        double totalLaneChanges = drivings.stream()
                .mapToDouble(d -> nz(d.laneChangePerKm()) * nz(d.distanceKm()))
                .sum();
        double lanePerKm = totalKmFromSource > 0 ? totalLaneChanges / totalKmFromSource : 0.0;

        Double postAccel = simpleMeanNullable(drivings, d -> d.postChangeAccel());
        Double sec0to40 = simpleMeanNullable(drivings, d -> d.sec0to40());
        Double avgDecel = simpleMeanNullable(drivings, d -> d.avgDecelRate());

        if (totalKmFromSource <= 0.0) {
            // Fallback: calculate from individual driving records
            for (String drivingId : drivingIds) {
                var record = drivingRepo.findByDrivingId(drivingId);
                if (record.isPresent() && record.get().getTotalDistance() != null) {
                    totalKmFromSource += record.get().getTotalDistance() / 1000.0;
                }
            }
        }

        var reacts = reactionRepo.findByDrivingIdIn(drivingIds);
        var rep = pickLatest(reacts);
        Long reactionMs = rep == null ? null : rep.getReactionMs();
        Boolean responded = rep == null ? null : rep.getResponded();
        Boolean decel = rep == null ? null : rep.getDecelOrStop();
        Boolean evasive = rep == null ? null : rep.getEvasiveManeuver();

        String A = compute.classifyA(sec0to40);
        String B = classifyB(avgDecel, totalKmFromSource);
        String C = compute.classifyC(nz(lanePerKm));
        String D = compute.classifyD(reactionMs, responded, decel, evasive);

        String code = compute.toCode(A, B, C, D);
        var radar = compute.toRadar(A, B, C, D);
        String headline = compute.headline(A, B, C, D);

        DnaSnapshot snap = snapshotRepo.findByReportId(reportId).orElse(
                DnaSnapshot.builder().reportId(reportId).userId(userId).build());
        snap.setStatus(status);
        snap.setCode(code);
        snap.setScoreA(radar.get("A"));
        snap.setScoreB(radar.get("B"));
        snap.setScoreC(radar.get("C"));
        snap.setScoreD(radar.get("D"));
        snap.setHeadline(headline);

        if (status == DnaSnapshot.Status.INTERIM && interimCount != null) {
            snap.setLastInterimCount(interimCount);
            snap.setLastInterimAt(LocalDateTime.now());
        }

        return snapshotRepo.save(snap);
    }

    private String classifyB(Double avgDecelRate, double totalKm) {
        if (avgDecelRate != null) {
            if (avgDecelRate >= 1.8)
                return "B3";
            if (avgDecelRate <= 0.8)
                return "B1";
            return "B2";
        }
        double hardBrakePerKm = 0.0;
        if (totalKm > 0) {
            hardBrakePerKm = 0.0 / totalKm;
        }
        return compute.classifyB(hardBrakePerKm);
    }

    private static AccidentReactionMetric pickLatest(List<AccidentReactionMetric> reacts) {
        if (reacts == null || reacts.isEmpty())
            return null;
        return reacts.stream()
                .filter(r -> r.getCreatedAt() != null)
                .max(Comparator.comparing(AccidentReactionMetric::getCreatedAt))
                .orElseGet(() -> reacts.get(0));
    }

    private static double nz(Double v) {
        return v == null ? 0.0 : v;
    }

    private static <T> Double simpleMeanNullable(Iterable<T> list, Function<T, Double> f) {
        double s = 0.0;
        int n = 0;
        for (T t : list) {
            Double v = f.apply(t);
            if (v != null && !v.isNaN() && !v.isInfinite()) {
                s += v;
                n++;
            }
        }
        return n == 0 ? null : (s / n);
    }
}