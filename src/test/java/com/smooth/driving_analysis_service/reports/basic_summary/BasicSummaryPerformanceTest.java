package com.smooth.driving_analysis_service.reports.basic_summary;

import com.smooth.driving_analysis_service.reports.basic_summary.dto.BasicSummaryResponse;
import com.smooth.driving_analysis_service.reports.basic_summary.entity.BasicSummary;
import com.smooth.driving_analysis_service.pipeline.entity.DrivingAccumulatedStats;
import com.smooth.driving_analysis_service.reports.basic_summary.repository.BasicSummaryRepository;
import com.smooth.driving_analysis_service.pipeline.repository.DrivingAccumulatedStatsRepository;
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

import java.math.BigDecimal;
import org.springframework.util.StopWatch;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BasicSummaryPerformanceTest {
    
    @Autowired
    private BasicSummaryService basicSummaryService;
    
    @Autowired
    private BasicSummaryRepository basicSummaryRepository;
    
    @Autowired
    private MilestoneReportRepository milestoneReportRepository;
    
    @Autowired
    private MilestoneItemRepository milestoneItemRepository;
    
    @Autowired
    private DrivingAccumulatedStatsRepository drivingAccumulatedStatsRepository;
    
    private static final Long TEST_USER_ID = 12345L;
    
    @BeforeEach
    void setUp() {
        // 테스트 데이터 정리
        basicSummaryRepository.deleteAll();
        milestoneItemRepository.deleteAll();
        milestoneReportRepository.deleteAll();
        drivingAccumulatedStatsRepository.deleteAll();
    }
    
    @Test
    @DisplayName("기본 요약 조회 성능 테스트 - 단일 요청")
    void getBasicSummary_PerformanceTest_SingleRequest() {
        // given
        MilestoneReport report = createMilestoneReport();
        createBasicSummarySnapshot(report.getId());
        
        StopWatch stopWatch = new StopWatch();
        
        // when
        stopWatch.start();
        BasicSummaryResponse response = basicSummaryService.getBasicSummary(report.getId());
        stopWatch.stop();
        
        // then
        assertThat(response).isNotNull();
        assertThat(response.getTotalDistanceKm()).isEqualTo(26.6);
        assertThat(stopWatch.getTotalTimeMillis()).isLessThan(100); // 100ms 이내
        
        System.out.println("단일 요청 처리 시간: " + stopWatch.getTotalTimeMillis() + "ms");
    }
    
    @Test
    @DisplayName("기본 요약 조회 성능 테스트 - 동시 요청")
    void getBasicSummary_PerformanceTest_ConcurrentRequests() throws Exception {
        // given
        MilestoneReport report = createMilestoneReport();
        createBasicSummarySnapshot(report.getId());
        
        int numberOfThreads = 10;
        int requestsPerThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        
        StopWatch stopWatch = new StopWatch();
        
        // when
        stopWatch.start();
        
        List<CompletableFuture<BasicSummaryResponse>> futures = new ArrayList<>();
        
        for (int i = 0; i < numberOfThreads; i++) {
            for (int j = 0; j < requestsPerThread; j++) {
                CompletableFuture<BasicSummaryResponse> future = CompletableFuture.supplyAsync(
                        () -> basicSummaryService.getBasicSummary(report.getId()),
                        executor
                );
                futures.add(future);
            }
        }
        
        // 모든 요청 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        stopWatch.stop();
        
        // then
        assertThat(futures).hasSize(numberOfThreads * requestsPerThread);
        
        // 모든 응답 검증
        for (CompletableFuture<BasicSummaryResponse> future : futures) {
            BasicSummaryResponse response = future.get();
            assertThat(response).isNotNull();
            assertThat(response.getTotalDistanceKm()).isEqualTo(26.6);
        }
        
        long totalTime = stopWatch.getTotalTimeMillis();
        double averageTime = (double) totalTime / (numberOfThreads * requestsPerThread);
        
        System.out.println("총 요청 수: " + (numberOfThreads * requestsPerThread));
        System.out.println("총 처리 시간: " + totalTime + "ms");
        System.out.println("평균 처리 시간: " + averageTime + "ms");
        
        assertThat(averageTime).isLessThan(50); // 평균 50ms 이내
        
        executor.shutdown();
    }
    
    @Test
    @DisplayName("스냅샷 생성 성능 테스트")
    void createSnapshot_PerformanceTest() {
        // given
        MilestoneReport report = createMilestoneReport();
        createLargeDrivingDataSet(report.getId(), 100); // 100개 주행 데이터
        
        StopWatch stopWatch = new StopWatch();
        
        // when - INTERIM 스냅샷 생성
        stopWatch.start();
        basicSummaryService.createOrUpdateInterimSnapshot(report.getId());
        stopWatch.stop();
        
        // then
        assertThat(stopWatch.getTotalTimeMillis()).isLessThan(1000); // 1초 이내
        
        System.out.println("INTERIM 스냅샷 생성 시간: " + stopWatch.getTotalTimeMillis() + "ms");
        
        // FINAL 스냅샷 생성 테스트
        stopWatch.start();
        basicSummaryService.createFinalSnapshot(report.getId());
        stopWatch.stop();
        
        assertThat(stopWatch.getTotalTimeMillis()).isLessThan(1000); // 1초 이내
        
        System.out.println("FINAL 스냅샷 생성 시간: " + stopWatch.getTotalTimeMillis() + "ms");
    }
    
    private MilestoneReport createMilestoneReport() {
        MilestoneReport report = MilestoneReport.builder()
                .userId(TEST_USER_ID)
                .cycleNo(1)
                .numberOfDriving(15)
                .status(MilestoneReport.Status.COMPLETED)
                .reportId("test-report-id")
                .build();
        return milestoneReportRepository.save(report);
    }
    
    private void createBasicSummarySnapshot(Long reportId) {
        BasicSummary summary = BasicSummary.builder()
                .reportId(reportId)
                .userId(TEST_USER_ID)
                .totalDistanceKm(BigDecimal.valueOf(26.6))
                .periodStart(LocalDate.of(2025, 8, 1))
                .periodEnd(LocalDate.of(2025, 8, 28))
                .averageDurationSec(BigDecimal.valueOf(38.25))
                .averageDistanceKm(BigDecimal.valueOf(1.77))
                .averageSpeedKmh(BigDecimal.valueOf(42.3))
                .averageCruiseRatio(BigDecimal.valueOf(0.684))
                .snapshotType(BasicSummary.SnapshotType.FINAL)
                .build();
        basicSummaryRepository.save(summary);
    }
    
    private void createLargeDrivingDataSet(Long reportId, int count) {
        for (int i = 1; i <= count; i++) {
            // DrivingAccumulatedStats 생성
            DrivingAccumulatedStats stats = DrivingAccumulatedStats.builder()
                    .userId(TEST_USER_ID)
                    .drivingId("trip-" + String.format("%05d", i))
                    .drivingMinutes(30 + (i % 60))
                    .totalDistance(15000 + (i * 100))
                    .laneChangeCount(3 + (i % 10))
                    .hardBrakeCount(i % 5)
                    .rapidAccelCount(1 + (i % 4))
                    .avgSpeed(30.0 + (i % 50))
                    .cruiseRatio(0.5 + ((i % 40) * 0.01))
                    .startTime(LocalDateTime.of(2025, 8, (i % 28) + 1, 10, 0))
                    .endTime(LocalDateTime.of(2025, 8, (i % 28) + 1, 11, 0))
                    .build();
            drivingAccumulatedStatsRepository.save(stats);
            
            // MilestoneItem 생성 - MilestoneReport 조회 필요
            MilestoneReport report = milestoneReportRepository.findById(reportId)
                    .orElseThrow(() -> new RuntimeException("Report not found"));
            
            MilestoneItem item = MilestoneItem.builder()
                    .reportId(report.getId())
                    .drivingId("trip-" + String.format("%05d", i))
                    .orderNo(i)
                    .build();
            milestoneItemRepository.save(item);
        }
    }
}