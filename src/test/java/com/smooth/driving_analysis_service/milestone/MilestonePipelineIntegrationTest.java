package com.smooth.driving_analysis_service.milestone;

import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneItem;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneItemRepository;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import com.smooth.driving_analysis_service.reports.milestone.service.MilestoneService;
import com.smooth.driving_analysis_service.reports.milestone.service.MilestoneServiceImpl;
import com.smooth.driving_analysis_service.trigger.dto.DrivingSummaryV1;
import com.smooth.driving_analysis_service.trigger.producer.ReportTriggerProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MilestonePipelineUnitTest {

    @Mock
    private MilestoneReportRepository milestoneReportRepository;

    @Mock
    private MilestoneItemRepository milestoneItemRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ReportTriggerProducer reportTriggerProducer;

    @InjectMocks
    private MilestoneServiceImpl milestoneService;

    private static final Long TEST_USER_ID = 12345L;

    @BeforeEach
    void setUp() {
        // Redis Mock 설정
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.get(anyString())).thenReturn(null); // 캐시 미스 시뮬레이션
    }

    @Test
    @DisplayName("1-3회 주행: 리포트 생성 및 아이템 누적")
    void testInitialDrivings() {
        // Given
        MilestoneReport mockReport = MilestoneReport.builder()
                .id(1L)
                .userId(TEST_USER_ID)
                .cycleNo(1)
                .numberOfDriving(0)
                .status(MilestoneReport.Status.COLLECTING)
                .build();

        when(milestoneReportRepository.findFirstByUserIdAndStatusOrderByIdDesc(TEST_USER_ID, MilestoneReport.Status.COLLECTING))
                .thenReturn(Optional.empty()) // 첫 번째 호출
                .thenReturn(Optional.of(mockReport)); // 이후 호출들

        when(milestoneReportRepository.save(any(MilestoneReport.class))).thenReturn(mockReport);

        // When: 3회 주행 처리
        for (int i = 1; i <= 3; i++) {
            milestoneService.processDrivingCompleted(TEST_USER_ID, "trip-" + String.format("%03d", i));
            mockReport.setNumberOfDriving(i); // 상태 업데이트 시뮬레이션
        }

        // Then
        verify(milestoneReportRepository, atLeastOnce()).save(any(MilestoneReport.class));
        verify(milestoneItemRepository, times(3)).save(any(MilestoneItem.class));
        verify(reportTriggerProducer, never()).emit(any()); // 4회 미만이므로 트리거 없음
    }

    @Test
    @DisplayName("4회 주행: 첫 번째 INTERIM 트리거 발생")
    void testFirstInterimTrigger() {
        // Given
        MilestoneReport mockReport = MilestoneReport.builder()
                .id(1L)
                .userId(TEST_USER_ID)
                .cycleNo(1)
                .numberOfDriving(4)
                .status(MilestoneReport.Status.COLLECTING)
                .build();

        // 4회째 주행 시 numberOfDriving이 4가 되도록 설정
        mockReport.setNumberOfDriving(3); // 3회 완료 상태에서 시작
        
        when(milestoneReportRepository.findFirstByUserIdAndStatusOrderByIdDesc(TEST_USER_ID, MilestoneReport.Status.COLLECTING))
                .thenReturn(Optional.of(mockReport));

        // When: 4회째 주행 처리 (3 -> 4로 증가)
        milestoneService.processDrivingCompleted(TEST_USER_ID, "trip-004");

        // Then: INTERIM 트리거 발생 확인 (4회 달성 시 트리거 발생)
        verify(reportTriggerProducer, times(1)).emit(any());
        verify(milestoneReportRepository, atLeastOnce()).save(any(MilestoneReport.class));
    }

    @Test
    @DisplayName("15회 주행: FINAL 트리거 발생 및 상태 변경")
    void testFinalTrigger() {
        // Given
        MilestoneReport mockReport = MilestoneReport.builder()
                .id(1L)
                .userId(TEST_USER_ID)
                .cycleNo(1)
                .numberOfDriving(15)
                .status(MilestoneReport.Status.PROCESSING) // 15회 달성 시 PROCESSING으로 변경
                .build();

        // 15회째 주행 시 numberOfDriving이 15가 되도록 설정
        mockReport.setNumberOfDriving(14); // 14회 완료 상태에서 시작
        
        when(milestoneReportRepository.findFirstByUserIdAndStatusOrderByIdDesc(TEST_USER_ID, MilestoneReport.Status.COLLECTING))
                .thenReturn(Optional.of(mockReport));
        when(milestoneItemRepository.findByReportIdOrderByOrderNoAsc(1L))
                .thenReturn(createMockItems(15));

        // When: 15회째 주행 처리 (14 -> 15로 증가)
        milestoneService.processDrivingCompleted(TEST_USER_ID, "trip-015");

        // Then: FINAL 트리거 발생 및 상태 변경 확인
        verify(reportTriggerProducer, times(1)).emit(any());
        verify(milestoneReportRepository, atLeastOnce()).save(any(MilestoneReport.class));
        verify(redisTemplate, times(1)).delete(anyString()); // active-report 캐시 삭제
    }

    @Test
    @DisplayName("16회 주행: 새 사이클 시작")
    void testNewCycleStart() {
        // Given: 15회 완료된 상태에서 16회째 주행
        MilestoneReport newCycleReport = MilestoneReport.builder()
                .id(2L)
                .userId(TEST_USER_ID)
                .cycleNo(2)
                .numberOfDriving(1)
                .status(MilestoneReport.Status.COLLECTING)
                .build();

        when(milestoneReportRepository.findFirstByUserIdAndStatusOrderByIdDesc(TEST_USER_ID, MilestoneReport.Status.COLLECTING))
                .thenReturn(Optional.empty()); // 활성 리포트 없음 (15회 완료로 PROCESSING 상태)
        when(milestoneReportRepository.findTopByUserIdOrderByCycleNoDesc(TEST_USER_ID))
                .thenReturn(Optional.of(MilestoneReport.builder().cycleNo(1).build()));
        when(milestoneReportRepository.save(any(MilestoneReport.class))).thenReturn(newCycleReport);

        // When: 16회째 주행 처리 (새 사이클 시작)
        milestoneService.processDrivingCompleted(TEST_USER_ID, "trip-016");

        // Then: 새 사이클 생성 확인 (새 리포트 생성 + 아이템 저장 = 2번 save 호출)
        verify(milestoneReportRepository, times(2)).save(any(MilestoneReport.class));
        verify(milestoneItemRepository, times(1)).save(any(MilestoneItem.class));
    }

    @Test
    @DisplayName("중복 주행 ID 처리")
    void testDuplicateDrivingId() {
        // Given
        MilestoneReport mockReport = MilestoneReport.builder()
                .id(1L)
                .userId(TEST_USER_ID)
                .cycleNo(1)
                .numberOfDriving(0)
                .status(MilestoneReport.Status.COLLECTING)
                .build();

        when(milestoneReportRepository.findFirstByUserIdAndStatusOrderByIdDesc(TEST_USER_ID, MilestoneReport.Status.COLLECTING))
                .thenReturn(Optional.of(mockReport));

        // When: 동일한 주행 ID로 2번 처리 (실제로는 중복 체크 없이 둘 다 저장됨)
        milestoneService.processDrivingCompleted(TEST_USER_ID, "trip-001");
        milestoneService.processDrivingCompleted(TEST_USER_ID, "trip-001"); // 중복

        // Then: 실제로는 둘 다 저장됨 (중복 체크 로직이 없음)
        verify(milestoneItemRepository, times(2)).save(any(MilestoneItem.class));
        verify(milestoneReportRepository, times(2)).save(any(MilestoneReport.class));
    }

    @Test
    @DisplayName("리포트 완료 처리")
    void testReportCompletion() {
        // Given
        MilestoneReport processingReport = MilestoneReport.builder()
                .id(1L)
                .userId(TEST_USER_ID)
                .status(MilestoneReport.Status.PROCESSING)
                .build();

        MilestoneReport completedReport = MilestoneReport.builder()
                .id(1L)
                .userId(TEST_USER_ID)
                .status(MilestoneReport.Status.COMPLETED)
                .build();

        when(milestoneReportRepository.findById(1L))
                .thenReturn(Optional.of(processingReport))
                .thenReturn(Optional.of(completedReport));
        when(milestoneReportRepository.save(any(MilestoneReport.class))).thenReturn(completedReport);

        // When: 리포트 완료 처리
        milestoneService.markReportCompleted(1L);

        // Then
        verify(milestoneReportRepository, times(1)).save(argThat(report -> 
            report.getStatus() == MilestoneReport.Status.COMPLETED
        ));
    }

    private List<MilestoneItem> createMockItems(int count) {
        return java.util.stream.IntStream.range(1, count + 1)
                .mapToObj(i -> MilestoneItem.builder()
                        .drivingId("trip-" + String.format("%03d", i))
                        .orderNo(i)
                        .build())
                .toList();
    }
}