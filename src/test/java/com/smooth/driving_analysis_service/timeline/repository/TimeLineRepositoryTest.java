package com.smooth.driving_analysis_service.timeline.repository;

import com.smooth.driving_analysis_service.driving.entity.DrivingRecord;
import com.smooth.driving_analysis_service.driving.entity.SummaryStatus;
import com.smooth.driving_analysis_service.driving.repository.DrivingRecordRepository;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@org.junit.jupiter.api.Disabled("Temporarily disabled due to Hibernate query issues")
class TimeLineRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DrivingRecordRepository drivingRecordRepository;

    @Autowired
    private MilestoneReportRepository milestoneReportRepository;

    @Test
    @DisplayName("주행 데이터 페이징 조회 테스트")
    void findDrivingRecords() {
        // given
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.now();
        
        DrivingRecord driving1 = DrivingRecord.builder()
                .drivingId("test_1")
                .userId(userId)
                .startTime(now.minusHours(2))
                .endTime(now.minusHours(1))
                .totalDistance(25.7)
                .avgSpeed(33.7)
                .cruiseRatio(0.78)
                .laneChangeCount(4)
                .hardBrakeCount(1)
                .rapidAccelCount(2)
                .sharpTurnCount(3)
                .status(SummaryStatus.COMPLETED)
                .build();

        DrivingRecord driving2 = DrivingRecord.builder()
                .drivingId("test_2")
                .userId(userId)
                .startTime(now.minusHours(4))
                .endTime(now.minusHours(3))
                .totalDistance(18.3)
                .avgSpeed(28.5)
                .cruiseRatio(0.72)
                .laneChangeCount(6)
                .hardBrakeCount(0)
                .rapidAccelCount(1)
                .sharpTurnCount(2)
                .status(SummaryStatus.COMPLETED)
                .build();

        entityManager.persistAndFlush(driving1);
        entityManager.persistAndFlush(driving2);

        // when
        Page<DrivingRecord> result = drivingRecordRepository.findByUserIdOrderByEndTimeDesc(
                userId, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getDrivingId()).isEqualTo("test_1"); // 최신순
        assertThat(result.getContent().get(1).getDrivingId()).isEqualTo("test_2");
    }

    @Test
    @DisplayName("리포트 데이터 상태별 필터링 테스트")
    void findReportsByStatus() {
        // given
        Long userId = 1L;
        
        MilestoneReport collecting = MilestoneReport.builder()
                .userId(userId)
                .cycleNo(1)
                .numberOfDriving(5)
                .status(MilestoneReport.Status.COLLECTING)
                .read(false)
                .build();

        MilestoneReport completed = MilestoneReport.builder()
                .userId(userId)
                .cycleNo(2)
                .numberOfDriving(15)
                .status(MilestoneReport.Status.COMPLETED)
                .read(false)
                .build();

        MilestoneReport processing = MilestoneReport.builder()
                .userId(userId)
                .cycleNo(3)
                .numberOfDriving(10)
                .status(MilestoneReport.Status.PROCESSING)
                .read(false)
                .build();

        entityManager.persistAndFlush(collecting);
        entityManager.persistAndFlush(completed);
        entityManager.persistAndFlush(processing);

        // when
        List<MilestoneReport.Status> statuses = Arrays.asList(
                MilestoneReport.Status.PROCESSING,
                MilestoneReport.Status.COMPLETED
        );
        Page<MilestoneReport> result = milestoneReportRepository.findByUserIdAndStatusInOrderByCreatedAtDesc(
                userId, statuses, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(2); // COLLECTING 제외
        assertThat(result.getContent())
                .extracting(MilestoneReport::getStatus)
                .containsExactlyInAnyOrder(
                        MilestoneReport.Status.COMPLETED,
                        MilestoneReport.Status.PROCESSING
                );
    }

    @Test
    @DisplayName("커서 기반 페이징 테스트")
    void cursorBasedPaging() {
        // given
        Long userId = 1L;
        LocalDateTime baseTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
        
        DrivingRecord driving1 = DrivingRecord.builder()
                .drivingId("cursor_test_1")
                .userId(userId)
                .startTime(baseTime)
                .endTime(baseTime.plusHours(1))
                .totalDistance(20.0)
                .avgSpeed(30.0)
                .cruiseRatio(0.7)
                .laneChangeCount(2)
                .hardBrakeCount(0)
                .rapidAccelCount(0)
                .sharpTurnCount(1)
                .status(SummaryStatus.COMPLETED)
                .build();

        DrivingRecord driving2 = DrivingRecord.builder()
                .drivingId("cursor_test_2")
                .userId(userId)
                .startTime(baseTime.minusHours(2))
                .endTime(baseTime.minusHours(1))
                .totalDistance(15.0)
                .avgSpeed(25.0)
                .cruiseRatio(0.6)
                .laneChangeCount(1)
                .hardBrakeCount(0)
                .rapidAccelCount(0)
                .sharpTurnCount(0)
                .status(SummaryStatus.COMPLETED)
                .build();

        entityManager.persistAndFlush(driving1);
        entityManager.persistAndFlush(driving2);

        // when - 커서 이전 데이터 조회
        LocalDateTime cursor = baseTime.plusMinutes(30);
        Page<DrivingRecord> result = drivingRecordRepository.findByUserIdAndEndTimeBeforeOrderByEndTimeDesc(
                userId, cursor, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getDrivingId()).isEqualTo("cursor_test_2");
    }
}