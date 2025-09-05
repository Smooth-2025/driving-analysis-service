package com.smooth.driving_analysis_service.milestone;

import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneItemRepository;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import com.smooth.driving_analysis_service.reports.milestone.service.MilestoneService;
import com.smooth.driving_analysis_service.trigger.dto.DrivingSummaryV1;
import com.smooth.driving_analysis_service.trigger.service.DrivingSummaryConsumerService;
import com.smooth.driving_analysis_service.config.TestAwsConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestAwsConfig.class)
class MilestonePipelineIntegrationTest {

    @Autowired
    private MilestoneService milestoneService;

    @Autowired
    private MilestoneReportRepository milestoneReportRepository;

    @Autowired
    private MilestoneItemRepository milestoneItemRepository;

    @MockBean
    private RedisTemplate<String, String> redisTemplate;

    private static final Long TEST_USER_ID = 12345L;

    @BeforeEach
    void setUp() {
        // Redis 캐시 정리 - 테스트에서는 Mock을 사용하므로 주석 처리
        // redisTemplate.getConnectionFactory().getConnection().flushAll();
        
        // 테스트 데이터 정리
        milestoneItemRepository.deleteAll();
        milestoneReportRepository.deleteAll();
    }

    @Test
    @DisplayName("1-3회 주행: 리포트 생성 및 아이템 누적")
    void testInitialDrivings() {
        // Given & When: 3회 주행 처리 - Redis 사용하지 않고 직접 마일스톤 서비스 호출
        for (int i = 1; i <= 3; i++) {
            DrivingSummaryV1 summary = createDrivingSummary("trip-" + String.format("%03d", i));
            // drivingSummaryConsumerService.handle(summary); // Redis 사용으로 주석 처리
            milestoneService.processDrivingCompleted(Long.parseLong(summary.getUserId()), summary.getDrivingId()); // 직접 마일스톤 서비스 호출
        }

        // Then
        Optional<MilestoneReport> activeReport = milestoneService.getActiveReport(TEST_USER_ID);
        assertThat(activeReport).isPresent();
        assertThat(activeReport.get().getNumberOfDriving()).isEqualTo(3);
        assertThat(activeReport.get().getStatus()).isEqualTo(MilestoneReport.Status.COLLECTING);
        assertThat(activeReport.get().getCycleNo()).isEqualTo(1);

        // 아이템 개수 확인
        int itemCount = milestoneItemRepository.countByReportId(activeReport.get().getId());
        assertThat(itemCount).isEqualTo(3);
    }

    @Test
    @DisplayName("4회 주행: 첫 번째 INTERIM 트리거 발생")
    void testFirstInterimTrigger() {
        // Given: 4회 주행 처리
        for (int i = 1; i <= 4; i++) {
            DrivingSummaryV1 summary = createDrivingSummary("trip-" + String.format("%03d", i));
            milestoneService.processDrivingCompleted(Long.parseLong(summary.getUserId()), summary.getDrivingId());
        }

        // Then
        Optional<MilestoneReport> activeReport = milestoneService.getActiveReport(TEST_USER_ID);
        assertThat(activeReport).isPresent();
        assertThat(activeReport.get().getNumberOfDriving()).isEqualTo(4);
        assertThat(activeReport.get().getStatus()).isEqualTo(MilestoneReport.Status.COLLECTING);

        // 4회 도달 시에도 여전히 COLLECTING 상태 (15회에만 PROCESSING으로 변경)
        int itemCount = milestoneItemRepository.countByReportId(activeReport.get().getId());
        assertThat(itemCount).isEqualTo(4);
    }

    @Test
    @DisplayName("15회 주행: FINAL 트리거 발생 및 상태 변경")
    void testFinalTrigger() {
        // Given: 15회 주행 처리
        for (int i = 1; i <= 15; i++) {
            DrivingSummaryV1 summary = createDrivingSummary("trip-" + String.format("%03d", i));
            milestoneService.processDrivingCompleted(Long.parseLong(summary.getUserId()), summary.getDrivingId());
        }

        // Then
        // 기존 리포트는 PROCESSING 상태로 변경됨
        var reports = milestoneReportRepository.findAllByUserIdOrderByCreatedAtDesc(TEST_USER_ID);
        assertThat(reports).hasSize(1);
        
        MilestoneReport completedReport = reports.get(0);
        assertThat(completedReport.getNumberOfDriving()).isEqualTo(15);
        assertThat(completedReport.getStatus()).isEqualTo(MilestoneReport.Status.PROCESSING);

        // 아이템 개수 확인
        int itemCount = milestoneItemRepository.countByReportId(completedReport.getId());
        assertThat(itemCount).isEqualTo(15);

        // 새로운 활성 리포트는 없어야 함 (16회차부터 새로 생성됨)
        Optional<MilestoneReport> activeReport = milestoneService.getActiveReport(TEST_USER_ID);
        assertThat(activeReport).isEmpty();
    }

    @Test
    @DisplayName("16회 주행: 새 사이클 시작")
    void testNewCycleStart() {
        // Given: 15회 완료 후 16회 주행
        for (int i = 1; i <= 16; i++) {
            DrivingSummaryV1 summary = createDrivingSummary("trip-" + String.format("%03d", i));
            milestoneService.processDrivingCompleted(Long.parseLong(summary.getUserId()), summary.getDrivingId());
        }

        // Then
        var reports = milestoneReportRepository.findAllByUserIdOrderByCreatedAtDesc(TEST_USER_ID);
        assertThat(reports).hasSize(2); // 첫 번째 사이클(완료) + 두 번째 사이클(진행중)

        // 첫 번째 사이클 (완료)
        MilestoneReport firstCycle = reports.stream()
                .filter(r -> r.getCycleNo() == 1)
                .findFirst().orElse(null);
        assertThat(firstCycle).isNotNull();
        assertThat(firstCycle.getNumberOfDriving()).isEqualTo(15);
        assertThat(firstCycle.getStatus()).isEqualTo(MilestoneReport.Status.PROCESSING);

        // 두 번째 사이클 (진행중)
        MilestoneReport secondCycle = reports.stream()
                .filter(r -> r.getCycleNo() == 2)
                .findFirst().orElse(null);
        assertThat(secondCycle).isNotNull();
        assertThat(secondCycle.getNumberOfDriving()).isEqualTo(1);
        assertThat(secondCycle.getStatus()).isEqualTo(MilestoneReport.Status.COLLECTING);
    }

    @Test
    @DisplayName("중복 주행 ID 처리")
    void testDuplicateDrivingId() {
        // Given: 동일한 주행 ID로 2번 처리
        DrivingSummaryV1 summary = createDrivingSummary("trip-001");
        
        // When
        milestoneService.processDrivingCompleted(Long.parseLong(summary.getUserId()), summary.getDrivingId());
        milestoneService.processDrivingCompleted(Long.parseLong(summary.getUserId()), summary.getDrivingId()); // 중복

        // Then
        Optional<MilestoneReport> activeReport = milestoneService.getActiveReport(TEST_USER_ID);
        assertThat(activeReport).isPresent();
        assertThat(activeReport.get().getNumberOfDriving()).isEqualTo(1); // 중복 제거됨

        int itemCount = milestoneItemRepository.countByReportId(activeReport.get().getId());
        assertThat(itemCount).isEqualTo(1);
    }

    @Test
    @DisplayName("리포트 완료 처리")
    void testReportCompletion() {
        // Given: 15회 주행으로 PROCESSING 상태 만들기
        for (int i = 1; i <= 15; i++) {
            DrivingSummaryV1 summary = createDrivingSummary("trip-" + String.format("%03d", i));
            milestoneService.processDrivingCompleted(Long.parseLong(summary.getUserId()), summary.getDrivingId());
        }

        var reports = milestoneReportRepository.findAllByUserIdOrderByCreatedAtDesc(TEST_USER_ID);
        MilestoneReport processingReport = reports.get(0);
        assertThat(processingReport.getStatus()).isEqualTo(MilestoneReport.Status.PROCESSING);

        // When: 리포트 완료 처리
        milestoneService.markReportCompleted(processingReport.getId());

        // Then
        MilestoneReport completedReport = milestoneReportRepository.findById(processingReport.getId()).orElse(null);
        assertThat(completedReport).isNotNull();
        assertThat(completedReport.getStatus()).isEqualTo(MilestoneReport.Status.COMPLETED);
    }

    private DrivingSummaryV1 createDrivingSummary(String drivingId) {
        DrivingSummaryV1 summary = new DrivingSummaryV1();
        summary.setV(1);
        summary.setUserId(String.valueOf(TEST_USER_ID));
        summary.setDrivingId(drivingId);
        summary.setStartedAt(String.valueOf(System.currentTimeMillis() - 3600000)); // 1시간 전
        summary.setEndedAt(String.valueOf(System.currentTimeMillis()));
        summary.setStatus("COMPLETED");
        summary.setProducer("test");
        summary.setDrivingMinutes(30);
        summary.setTotalDistance(15000);
        summary.setLaneChangeCount(3);
        summary.setHardBrakeCount(1);
        summary.setRapidAccelCount(2);
        return summary;
    }
}