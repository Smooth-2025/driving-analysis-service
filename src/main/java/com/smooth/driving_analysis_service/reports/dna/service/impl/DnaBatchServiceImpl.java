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

    private final DrivingRecordRepository drivingRepo;                      // 분모/폴백용
    private final AccidentReactionMetricRepository reactionRepo;            // D축(RDS)

    private final DnaSnapshotRepository snapshotRepo;
    private final DnaComputeService compute;
    private final DnaMetricSource metricSource;                             // A/B/C(원천)

    @Override
    @Transactional
    public DnaSnapshot runInterim(Long reportId) {
        MilestoneReport report = reportRepo.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("report not found: " + reportId));
        
        // COLLECTING 상태가 아니면 Interim 실행 안함
        if (report.getStatus() != MilestoneReport.Status.COLLECTING) {
            throw new IllegalStateException("Interim only for COLLECTING status, current: " + report.getStatus());
        }
        
        int currentCount = report.getNumberOfDriving();
        
        // 임계 미만(1~3개): Interim 실행 안 함
        if (currentCount < 4) {
            throw new IllegalStateException("Interim requires at least 4 driving records, current: " + currentCount);
        }
        
        // 4의 배수 구간(4/8/12)에서만 실행
        int targetInterimCount = (currentCount / 4) * 4; // 가장 큰 4의 배수
        if (targetInterimCount > 12) targetInterimCount = 12; // 최대 12까지만
        
        // 이미 처리된 구간인지 확인
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
        
        // PROCESSING 상태가 아니면 Final 실행 안함
        if (report.getStatus() != MilestoneReport.Status.PROCESSING) {
            throw new IllegalStateException("Final only for PROCESSING status, current: " + report.getStatus());
        }
        
        // 정확히 15개가 아니면 실행 안함
        if (report.getNumberOfDriving() != 15) {
            throw new IllegalStateException("Final requires exactly 15 driving records, current: " + report.getNumberOfDriving());
        }
        
        DnaSnapshot result = upsert(reportId, DnaSnapshot.Status.FINAL, null);
        
        // 상태를 COMPLETED로 전환
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

        // ===== A/B/C: 원천 로그에서 per-driving 메트릭 로딩 =====
        var input = metricSource.loadForReport(reportId, drivingIds);

        double totalKmFromSource = sum(input.drivings(), d -> nz(d.distanceKm()));
        double lanePerKm = weightedMean(input.drivings(), DnaMetricSource.PerDriving::laneChangePerKm, d -> nz(d.distanceKm()));
        Double postAccel = simpleMeanNullable(input.drivings(), DnaMetricSource.PerDriving::postChangeAccel);
        Double sec0to40 = simpleMeanNullable(input.drivings(), DnaMetricSource.PerDriving::sec0to40);
        Double avgDecel = simpleMeanNullable(input.drivings(), DnaMetricSource.PerDriving::avgDecelRate);

        // 폴백: 거리 분모가 없으면 RDS 요약으로 보조
        if (totalKmFromSource <= 0.0) {
            List<DrivingRecord> trips = drivingRepo.findByDrivingIdIn(drivingIds);
            totalKmFromSource = trips.stream().mapToDouble(DrivingRecord::getTotalDistance).sum();
        }

        // ===== D: 사고 반응 대표값(최신 1건) =====
        var reacts = reactionRepo.findByDrivingIdIn(drivingIds);
        var rep = pickLatest(reacts);
        Long reactionMs = rep == null ? null : rep.getReactionMs();
        Boolean responded = rep == null ? null : rep.getResponded();
        Boolean decel = rep == null ? null : rep.getDecelOrStop();
        Boolean evasive = rep == null ? null : rep.getEvasiveManeuver();

        // ===== 분류 =====
        String A = compute.classifyA(sec0to40);
        String B = classifyB(avgDecel, totalKmFromSource);      // Δv/Δt 우선, 폴백은 compute.classifyB()
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
        
        // 메타데이터 업데이트
        if (status == DnaSnapshot.Status.INTERIM && interimCount != null) {
            snap.setLastInterimCount(interimCount);
            snap.setLastInterimAt(LocalDateTime.now());
        }

        return snapshotRepo.save(snap);
    }

    private String classifyB(Double avgDecelRate, double totalKm) {
        if (avgDecelRate != null) {
            // 임계치는 실측 데이터 보며 튜닝
            if (avgDecelRate >= 1.8) return "B3";   // 급감속형
            if (avgDecelRate <= 0.8) return "B1";   // 사전 감속형
            return "B2";                            // 중간
        }
        // 폴백: 하드브레이크/거리 기반 근사치가 필요하면 DrivingRecord 합계 사용해 compute.classifyB() 호출
        double hardBrakePerKm = 0.0;
        if (totalKm > 0) hardBrakePerKm = 0.0 / totalKm;  // TODO: 필요 시 RDS 이벤트 합계 주입
        return compute.classifyB(hardBrakePerKm);
    }

    private static AccidentReactionMetric pickLatest(List<AccidentReactionMetric> reacts) {
        if (reacts == null || reacts.isEmpty()) return null;
        return reacts.stream()
                .filter(r -> r.getCreatedAt() != null)
                .max(Comparator.comparing(AccidentReactionMetric::getCreatedAt))
                .orElseGet(() -> reacts.get(0));
    }

    // ===== 수학 유틸 =====
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
