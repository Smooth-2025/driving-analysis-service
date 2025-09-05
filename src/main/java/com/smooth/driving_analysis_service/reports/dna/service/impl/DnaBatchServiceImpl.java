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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class DnaBatchServiceImpl implements DnaBatchService {

    private final MilestoneReportRepository reportRepo;
    private final MilestoneItemRepository itemRepo;

    private final DrivingRecordRepository drivingRepo;                      // Î∂ÑÎ™®/?¥Î∞±??
    private final AccidentReactionMetricRepository reactionRepo;            // DÏ∂?RDS)

    private final DnaSnapshotRepository snapshotRepo;
    private final DnaComputeService compute;
    private final DnaMetricSource metricSource;                             // A/B/C(?êÏ≤ú)

    @Override
    @Transactional
    public DnaSnapshot runInterim(Long reportId) {
        MilestoneReport report = reportRepo.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("report not found: " + reportId));
        
        // COLLECTING ?ÅÌÉúÍ∞Ä ?ÑÎãàÎ©?Interim ?§Ìñâ ?àÌï®
        if (report.getStatus() != MilestoneReport.Status.COLLECTING) {
            throw new IllegalStateException("Interim only for COLLECTING status, current: " + report.getStatus());
        }
        
        int currentCount = report.getNumberOfDriving();
        
        // ?ÑÍ≥Ñ ÎØ∏Îßå(1~3Í∞?: Interim ?§Ìñâ ????
        if (currentCount < 4) {
            throw new IllegalStateException("Interim requires at least 4 driving records, current: " + currentCount);
        }
        
        // 4??Î∞∞Ïàò Íµ¨Í∞Ñ(4/8/12)?êÏÑúÎß??§Ìñâ
        int targetInterimCount = (currentCount / 4) * 4; // Í∞Ä????4??Î∞∞Ïàò
        if (targetInterimCount > 12) targetInterimCount = 12; // ÏµúÎ? 12ÍπåÏ?Îß?
        
        // ?¥Î? Ï≤òÎ¶¨??Íµ¨Í∞Ñ?∏Ï? ?ïÏù∏
        DnaSnapshot existing = snapshotRepo.findByReportId(reportId).orElse(null);
        if (existing != null && existing.getLastInterimCount() != null && 
            existing.getLastInterimCount() >= targetInterimCount) {
            return existing; // ?¥Î? Ï≤òÎ¶¨??
        }
        
        return upsert(reportId, DnaSnapshot.Status.INTERIM, targetInterimCount);
    }

    @Override
    @Transactional
    public DnaSnapshot runFinal(Long reportId) {
        MilestoneReport report = reportRepo.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("report not found: " + reportId));
        
        // PROCESSING ?ÅÌÉúÍ∞Ä ?ÑÎãàÎ©?Final ?§Ìñâ ?àÌï®
        if (report.getStatus() != MilestoneReport.Status.PROCESSING) {
            throw new IllegalStateException("Final only for PROCESSING status, current: " + report.getStatus());
        }
        
        // ?ïÌôï??15Í∞úÍ? ?ÑÎãàÎ©??§Ìñâ ?àÌï®
        if (report.getNumberOfDriving() != 15) {
            throw new IllegalStateException("Final requires exactly 15 driving records, current: " + report.getNumberOfDriving());
        }
        
        DnaSnapshot result = upsert(reportId, DnaSnapshot.Status.FINAL, null);
        
        // ?ÅÌÉúÎ•?COMPLETEDÎ°??ÑÌôò
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
        if (drivingIds.isEmpty()) throw new IllegalStateException("no drivingIds for report " + reportId);

        // ===== A/B/C: ?êÏ≤ú Î°úÍ∑∏?êÏÑú per-driving Î©îÌä∏Î¶?Î°úÎî© =====
        var input = metricSource.loadForReport(reportId, drivingIds);

        double totalKmFromSource = sum(input.drivings(), d -> nz(d.distanceKm()));
        double lanePerKm = weightedMean(input.drivings(), DnaMetricSource.PerDriving::laneChangePerKm, d -> nz(d.distanceKm()));
        Double postAccel = simpleMeanNullable(input.drivings(), DnaMetricSource.PerDriving::postChangeAccel);
        Double sec0to40 = simpleMeanNullable(input.drivings(), DnaMetricSource.PerDriving::sec0to40);
        Double avgDecel = simpleMeanNullable(input.drivings(), DnaMetricSource.PerDriving::avgDecelRate);

        // ?¥Î∞±: Í±∞Î¶¨ Î∂ÑÎ™®Í∞Ä ?ÜÏúºÎ©?RDS ?îÏïΩ?ºÎ°ú Î≥¥Ï°∞
        if (totalKmFromSource <= 0.0) {
            List<DrivingRecord> trips = drivingRepo.findByDrivingIdIn(drivingIds);
            totalKmFromSource = trips.stream().mapToDouble(DrivingRecord::getTotalDistance).sum();
        }

        // ===== D: ?¨Í≥† Î∞òÏùë ?Ä?úÍ∞í(ÏµúÏã† 1Í±? =====
        var reacts = reactionRepo.findByDrivingIdIn(drivingIds);
        var rep = pickLatest(reacts);
        Long reactionMs = rep == null ? null : rep.getResponseTimeMs();
        Boolean responded = rep == null ? null : rep.getResponded();
        Boolean decel = rep == null ? null : rep.getDecelOrStop();
        Boolean evasive = rep == null ? null : rep.getEvasiveManeuver();

        // ===== Î∂ÑÎ•ò =====
        String A = compute.classifyA(sec0to40);
        String B = classifyB(avgDecel, totalKmFromSource);      // ?v/?t ?∞ÏÑ†, ?¥Î∞±?Ä compute.classifyB()
        String C = compute.classifyC(nz(lanePerKm) /*, postAccel */);
        String D = compute.classifyD(reactionMs, responded, decel, evasive);

        String code = compute.toCode(A, B, C, D);
        var radar = compute.toRadar(A, B, C, D);
        String headline = compute.headline(A, B, C, D);

        DnaSnapshot snap = snapshotRepo.findByReportId(reportId).orElse(
                DnaSnapshot.builder().reportId(reportId).userId(userId).build()
        );
        snap.setStatus(status);
        snap.setCode(code);
        snap.setScoreA(radar.get("A"));
        snap.setScoreB(radar.get("B"));
        snap.setScoreC(radar.get("C"));
        snap.setScoreD(radar.get("D"));
        snap.setHeadline(headline);
        
        // Î©îÌ??∞Ïù¥???ÖÎç∞?¥Ìä∏
        if (status == DnaSnapshot.Status.INTERIM && interimCount != null) {
            snap.setLastInterimCount(interimCount);
            snap.setLastInterimAt(LocalDateTime.now());
        }

        return snapshotRepo.save(snap);
    }

    private String classifyB(Double avgDecelRate, double totalKm) {
        if (avgDecelRate != null) {
            // ?ÑÍ≥ÑÏπòÎäî ?§Ï∏° ?∞Ïù¥??Î≥¥Î©∞ ?úÎãù
            if (avgDecelRate >= 1.8) return "B3";   // Í∏âÍ∞ê?çÌòï
            if (avgDecelRate <= 0.8) return "B1";   // ?¨Ï†Ñ Í∞êÏÜç??
            return "B2";                            // Ï§ëÍ∞Ñ
        }
        // ?¥Î∞±: ?òÎìúÎ∏åÎ†à?¥ÌÅ¨/Í±∞Î¶¨ Í∏∞Î∞ò Í∑ºÏÇ¨ÏπòÍ? ?ÑÏöî?òÎ©¥ DrivingRecord ?©Í≥Ñ ?¨Ïö©??compute.classifyB() ?∏Ï∂ú
        double hardBrakePerKm = 0.0;
        if (totalKm > 0) hardBrakePerKm = 0.0 / totalKm;  // TODO: ?ÑÏöî ??RDS ?¥Î≤§???©Í≥Ñ Ï£ºÏûÖ
        return compute.classifyB(hardBrakePerKm);
    }

    private static AccidentReactionMetric pickLatest(List<AccidentReactionMetric> reacts) {
        if (reacts == null || reacts.isEmpty()) return null;
        return reacts.stream()
                .filter(r -> r.getCreatedAt() != null)
                .max(Comparator.comparing(AccidentReactionMetric::getCreatedAt))
                .orElseGet(() -> reacts.get(0));
    }

    // ===== ?òÌïô ?†Ìã∏ =====
    private static double nz(Double v) { return v == null ? 0.0 : v; }

    private static <T> double sum(Iterable<T> list, Function<T, Double> f) {
        double s = 0.0;
        for (T t : list) s += nz(f.apply(t));
        return s;
    }

    private static <T> Double simpleMeanNullable(Iterable<T> list, Function<T, Double> f) {
        double s = 0.0; int n = 0;
        for (T t : list) {
            Double v = f.apply(t);
            if (v != null && !v.isNaN() && !v.isInfinite()) { s += v; n++; }
        }
        return n == 0 ? null : (s / n);
    }

    private static <T> double weightedMean(Iterable<T> list, Function<T, Double> valueFn, Function<T, Double> weightFn) {
        double num = 0.0, den = 0.0;
        for (T t : list) {
            Double v = valueFn.apply(t);
            Double w = weightFn.apply(t);
            if (v == null || w == null || w <= 0) continue;
            num += v * w;
            den += w;
        }
        return den == 0.0 ? 0.0 : (num / den);
    }
}
