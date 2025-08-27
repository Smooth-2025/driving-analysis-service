package com.smooth.driving_analysis_service.reports.milestone.service;

import com.smooth.driving_analysis_service.driving.entity.DrivingRecord;
import com.smooth.driving_analysis_service.driving.entity.SummaryStatus;
import com.smooth.driving_analysis_service.driving.repository.DrivingRecordRepository;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneItem;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneItemRepository;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MilestoneMaterializeService {

    private final DrivingRecordRepository drivingRecordRepository;
    private final MilestoneReportRepository reportRepository;
    private final MilestoneItemRepository itemRepository;

    /**
     * 15회 배수 도달 시점에 "최근 15건(완주)"을 스냅샷 테이블에 저장(멱등).
     * - 엔티티 변경 없이, 레포 메서드(시그니처)에만 맞춰 구현
     * - report(header) upsert + item(detail) UNIQUE(report_id, driving_id)로 중복 방지
     */
    @Transactional
    public MaterializeResult materializeLatest15(Long userId) {
        // 총합: 현재 레포 시그니처 기준 (COMPLETED만으로 카운트하려면 countByUserIdAndStatus 추가 후 교체 권장)
        long total = drivingRecordRepository.countByUserId(userId);
        if (total == 0 || total % 15 != 0) {
            return MaterializeResult.notReady(userId, total);
        }

        int cycleNo = (int) (total / 15);

        // report 헤더 upsert (userId+cycleNo 유니크 가정)
        MilestoneReport report = reportRepository.findByUserIdAndCycleNo(userId, cycleNo)
                .orElseGet(() -> reportRepository.save(
                        MilestoneReport.builder()
                                .userId(userId)
                                .cycleNo(cycleNo)
                                .totalTrips((int) total)
                                .isRead(false)
                                .build()
                ));

        // 최신 15건: "완주(COMPLETED)"만 대상
        List<DrivingRecord> latest15 =
                drivingRecordRepository.findTop15ByUserIdAndStatusOrderByEndTimeDesc(userId, SummaryStatus.COMPLETED);

        int inserted = 0;
        int skipped = 0;
        int order = 1;

        for (DrivingRecord dr : latest15) {
            String drivingId = dr.getDrivingId();
            if (drivingId == null) {
                // driving_id가 비어있으면 스냅샷 품질을 위해 스킵
                skipped++;
                continue;
            }

            // 멱등: 이미 있으면 스킵
            if (itemRepository.existsByReportIdAndDrivingId(report.getId(), drivingId)) {
                skipped++;
            } else {
                try {
                    itemRepository.save(
                            MilestoneItem.builder()
                                    .reportId(report.getId())
                                    .drivingId(drivingId)
                                    .orderNo(order)
                                    .build()
                    );
                    inserted++;
                } catch (DataIntegrityViolationException e) {
                    // 동시성/중복 삽입 등 UNIQUE 제약 위반 시 안전 스킵
                    skipped++;
                }
            }
            order++;
        }

        int currentItems = (int) itemRepository.countByReportId(report.getId());
        return MaterializeResult.ready(userId, report.getId(), (int) total, inserted, skipped, currentItems);
    }

    // 응답 DTO(디버그 컨트롤러와 연동)
    public record MaterializeResult(
            Long userId,
            Long reportId,
            int totalTrips,
            boolean ready,
            int inserted,
            int skipped,
            int currentItems
    ) {
        public static MaterializeResult notReady(Long userId, long totalTrips) {
            return new MaterializeResult(userId, null, (int) totalTrips, false, 0, 0, 0);
        }
        public static MaterializeResult ready(
                Long userId, Long reportId, int totalTrips, int inserted, int skipped, int currentItems
        ) {
            return new MaterializeResult(userId, reportId, totalTrips, true, inserted, skipped, currentItems);
        }
    }
}
