package com.smooth.driving_analysis_service.reports.basic_summary;

import com.smooth.driving_analysis_service.pipeline.entity.DrivingAccumulatedStats;
import com.smooth.driving_analysis_service.pipeline.repository.DrivingAccumulatedStatsRepository;
import com.smooth.driving_analysis_service.reports.basic_summary.entity.BasicSummary;
import com.smooth.driving_analysis_service.reports.basic_summary.repository.BasicSummaryRepository;
import com.smooth.driving_analysis_service.reports.basic_summary.service.BasicSummaryService;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneItem;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneItemRepository;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BasicSummaryIntegrationFullTest {

    @Autowired
    private BasicSummaryService basicSummaryService;

    @Autowired
    private BasicSummaryRepository basicSummaryRepository;

    @Autowired
    private DrivingAccumulatedStatsRepository drivingAccumulatedStatsRepository;

    @Autowired
    private MilestoneReportRepository milestoneReportRepository;

    @Autowired
    private MilestoneItemRepository milestoneItemRepository;

    private Long testReportId;
    private Long testUserId = 12345L;

    @BeforeEach
    void setUp() {
        // 1. 마일스톤 리포트 생성
        MilestoneReport milestoneReport = MilestoneReport.builder()
                .userId(testUserId)
                .cycleNo(1)
                .numberOfDriving(4)
                .status(MilestoneReport.Status.COLLECTING)
                .build();
        
        MilestoneReport savedReport = milestoneReportRepository.save(milestoneReport);
        testReportId = savedReport.getId();

        // 2. 누적 통계 데이터 생성 (4회 주행)
        createAccumulatedStats("trip-001", 30, 15000, 30.0, 0.75);
        createAccumulatedStats("trip-002", 25, 12000, 28.8, 0.70);
        createAccumulatedStats("trip-003", 40, 20000, 30.0, 0.80);
        createAccumulatedStats("trip-004", 35, 18000, 30.9, 0.65);

        // 3. 마일스톤 아이템 생성
        createMilestoneItem("trip-001", 1);
        createMilestoneItem("trip-002", 2);
        createMilestoneItem("trip-003", 3);
        createMilestoneItem("trip-004", 4);
    }

    @Test
    @DisplayName("INTERIM 스냅샷 생성 및 조회 통합 테스트")
    void interimSnapshotIntegrationTest() {
        // when - INTERIM 스냅샷 생성
        basicSummaryService.createOrUpdateInterimSnapshot(testReportId);

        // then - 스냅샷 생성 확인
        List<BasicSummary> snapshots = basicSummaryRepository.findAll();
        assertThat(snapshots).hasSize(1);

        BasicSummary interimSnapshot = snapshots.get(0);
        assertThat(interimSnapshot.getSnapshotType()).isEqualTo(BasicSummary.SnapshotType.INTERIM);
        assertThat(interimSnapshot.getReportId()).isEqualTo(testReportId);
        assertThat(interimSnapshot.getUserId()).isEqualTo(testUserId);

        // 계산 검증 (총 거리: 65km, 평균 시간: 32.5분 = 1950초)
        assertThat(interimSnapshot.getTotalDistanceKm().doubleValue()).isEqualTo(65.0);
        assertThat(interimSnapshot.getAverageDurationSec().doubleValue()).isEqualTo(1950.0); // 32.5분 * 60초
        assertThat(interimSnapshot.getAverageDistanceKm().doubleValue()).isEqualTo(16.25); // 65/4
        assertThat(interimSnapshot.getAverageSpeedKmh().doubleValue()).isCloseTo(30.175, org.assertj.core.data.Offset.offset(0.01));
        assertThat(interimSnapshot.getAverageCruiseRatio().doubleValue()).isEqualTo(0.725); // (0.75+0.70+0.80+0.65)/4
    }

    @Test
    @DisplayName("INTERIM 스냅샷 갱신 테스트")
    void interimSnapshotUpdateTest() {
        // given - 첫 번째 INTERIM 스냅샷 생성
        basicSummaryService.createOrUpdateInterimSnapshot(testReportId);
        assertThat(basicSummaryRepository.findAll()).hasSize(1);

        // when - 두 번째 INTERIM 스냅샷 생성 (갱신)
        basicSummaryService.createOrUpdateInterimSnapshot(testReportId);

        // then - 스냅샷이 교체되었는지 확인 (여전히 1개)
        List<BasicSummary> snapshots = basicSummaryRepository.findAll();
        assertThat(snapshots).hasSize(1);
        assertThat(snapshots.get(0).getSnapshotType()).isEqualTo(BasicSummary.SnapshotType.INTERIM);
    }

    @Test
    @DisplayName("FINAL 스냅샷 생성 테스트")
    void finalSnapshotTest() {
        // when - FINAL 스냅샷 생성
        basicSummaryService.createFinalSnapshot(testReportId);

        // then - FINAL 스냅샷 확인
        List<BasicSummary> snapshots = basicSummaryRepository.findAll();
        assertThat(snapshots).hasSize(1);

        BasicSummary finalSnapshot = snapshots.get(0);
        assertThat(finalSnapshot.getSnapshotType()).isEqualTo(BasicSummary.SnapshotType.FINAL);
        assertThat(finalSnapshot.getTotalDistanceKm().doubleValue()).isEqualTo(65.0);
    }

    @Test
    @DisplayName("INTERIM과 FINAL 스냅샷 공존 테스트")
    void interimAndFinalCoexistenceTest() {
        // given - INTERIM 스냅샷 생성
        basicSummaryService.createOrUpdateInterimSnapshot(testReportId);

        // when - FINAL 스냅샷 추가 생성
        basicSummaryService.createFinalSnapshot(testReportId);

        // then - 두 스냅샷 모두 존재
        List<BasicSummary> snapshots = basicSummaryRepository.findAll();
        assertThat(snapshots).hasSize(2);

        boolean hasInterim = snapshots.stream()
                .anyMatch(s -> s.getSnapshotType() == BasicSummary.SnapshotType.INTERIM);
        boolean hasFinal = snapshots.stream()
                .anyMatch(s -> s.getSnapshotType() == BasicSummary.SnapshotType.FINAL);

        assertThat(hasInterim).isTrue();
        assertThat(hasFinal).isTrue();
    }

    private void createAccumulatedStats(String drivingId, int drivingMinutes, int totalDistance, 
                                      double avgSpeed, double cruiseRatio) {
        LocalDateTime now = LocalDateTime.now();
        
        DrivingAccumulatedStats stats = DrivingAccumulatedStats.builder()
                .userId(testUserId)
                .drivingId(drivingId)
                .drivingMinutes(drivingMinutes)
                .totalDistance(totalDistance)
                .laneChangeCount(3)
                .hardBrakeCount(1)
                .rapidAccelCount(2)
                .avgSpeed(avgSpeed)
                .cruiseRatio(cruiseRatio)
                .startTime(now.minusHours(2))
                .endTime(now.minusHours(1))
                .build();

        drivingAccumulatedStatsRepository.save(stats);
    }

    private void createMilestoneItem(String drivingId, int orderNo) {
        MilestoneItem item = MilestoneItem.builder()
                .reportId(testReportId)
                .drivingId(drivingId)
                .orderNo(orderNo)
                .build();

        milestoneItemRepository.save(item);
    }
}