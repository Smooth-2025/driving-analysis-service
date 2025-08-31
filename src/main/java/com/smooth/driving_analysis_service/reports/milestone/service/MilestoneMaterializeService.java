package com.smooth.driving_analysis_service.reports.milestone.service;

import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneItemRepository;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MilestoneMaterializeService {

    private final MilestoneReportRepository reportRepository;
    private final MilestoneItemRepository itemRepository;

    /** 컨트롤러에서 제네릭으로 참조하는 타입 – 없어서 컴파일 에러났던 타입 */
    public static record MaterializeResult(
            Long reportId,
            int itemCount,
            MilestoneReport.Status status
    ) {}

    /**
     * 디버그/강제 머티리얼라이즈용 예시 메서드
     * - userId의 "COLLECTING" 리포트를 찾아 현재 아이템 개수/상태 리턴
     * - 필요하면 여기에 중간통계 집계/플래그 갱신 로직을 추가하세요.
     */
    @Transactional(readOnly = true)
    public MaterializeResult materialize(Long userId) {
        var reportOpt = reportRepository
                .findFirstByUserIdAndStatusOrderByIdDesc(userId, MilestoneReport.Status.COLLECTING);

        if (reportOpt.isEmpty()) {
            // COLLECTING 리포트가 없을 때는 빈 결과 리턴(상황에 따라 예외로 바꿔도 됨)
            return new MaterializeResult(null, 0, MilestoneReport.Status.FINALIZED);
        }

        var report = reportOpt.get();
        int count = itemRepository.countByReportId(report.getId());

        return new MaterializeResult(report.getId(), count, report.getStatus());
    }
}
