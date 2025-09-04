package com.smooth.driving_analysis_service.trigger.service;

import com.smooth.driving_analysis_service.global.redis.RedisKeys;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneItem;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneItemRepository;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import com.smooth.driving_analysis_service.trigger.dto.DrivingSummaryV1;
import com.smooth.driving_analysis_service.trigger.dto.ReportTriggerV1;
import com.smooth.driving_analysis_service.trigger.producer.ReportTriggerProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DrivingSummaryConsumerServiceTest {

    @Mock
    private RedisTemplate<String, String> redis;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private MilestoneReportRepository reportRepo;

    @Mock
    private MilestoneItemRepository itemRepo;

    @Mock
    private ReportTriggerProducer producer;

    @InjectMocks
    private DrivingSummaryConsumerService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "threshold", 15);
        lenient().when(redis.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testProcessDrivingSummary_NotCompleted() {
        // Given
        DrivingSummaryV1 summary = new DrivingSummaryV1();
        summary.setStatus("PROCESSING");

        // When
        service.processDrivingSummary("msg-123", summary);

        // Then
        verifyNoInteractions(valueOperations, reportRepo, itemRepo, producer);
    }

    @Test
    void testProcessDrivingSummary_AlreadyProcessed() {
        // Given
        DrivingSummaryV1 summary = new DrivingSummaryV1();
        summary.setStatus("COMPLETED");
        summary.setDrivingId("driving-123");

        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class)))
            .thenReturn(false);

        // When
        service.processDrivingSummary("msg-123", summary);

        // Then
        verify(valueOperations).setIfAbsent(
            eq(RedisKeys.processedTrip("driving-123")), 
            eq("1"), 
            eq(Duration.ofDays(7))
        );
        verifyNoInteractions(reportRepo, itemRepo, producer);
    }

    @Test
    void testProcessDrivingSummary_NewTrip_BelowThreshold() {
        // Given
        DrivingSummaryV1 summary = createValidSummary();
        MilestoneReport report = createMockReport(1L, 10);

        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class)))
            .thenReturn(true);
        when(valueOperations.get(RedisKeys.activeReportForUser("123")))
            .thenReturn("1");
        when(reportRepo.findById(1L)).thenReturn(Optional.of(report));
        when(itemRepo.existsByReportIdAndDrivingId(1L, "driving-123"))
            .thenReturn(false);
        when(itemRepo.countByReportId(1L)).thenReturn(10, 11);

        // When
        service.processDrivingSummary("msg-123", summary);

        // Then
        verify(itemRepo).save(any(MilestoneItem.class));
        verify(reportRepo).save(report);
        verify(producer, never()).emit(any());
        assertEquals(11, report.getNumberOfDriving());
    }

    @Test
    void testProcessDrivingSummary_ReachesThreshold() {
        // Given
        DrivingSummaryV1 summary = createValidSummary();
        MilestoneReport report = createMockReport(1L, 14);

        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class)))
            .thenReturn(true);
        when(valueOperations.get(RedisKeys.activeReportForUser("123")))
            .thenReturn("1");
        when(reportRepo.findById(1L)).thenReturn(Optional.of(report));
        when(itemRepo.existsByReportIdAndDrivingId(1L, "driving-123"))
            .thenReturn(false);
        when(itemRepo.countByReportId(1L)).thenReturn(14, 15);
        when(itemRepo.findByReportIdOrderByOrderNoAsc(1L))
            .thenReturn(createMockItems());

        // When
        service.processDrivingSummary("msg-123", summary);

        // Then
        verify(itemRepo).save(any(MilestoneItem.class));
        verify(reportRepo, times(2)).save(report); // 한 번은 item 추가 시, 한 번은 상태 변경 시
        verify(redis).delete(RedisKeys.activeReportForUser("123"));
        
        ArgumentCaptor<ReportTriggerV1> triggerCaptor = ArgumentCaptor.forClass(ReportTriggerV1.class);
        verify(producer).emit(triggerCaptor.capture());
        
        ReportTriggerV1 trigger = triggerCaptor.getValue();
        assertEquals("123", trigger.getUserId());
        assertEquals(1L, trigger.getReportId());
        assertEquals(15, trigger.getMilestone());
        assertEquals("PROCESSING", trigger.getStatus());
        assertEquals(MilestoneReport.Status.PROCESSING, report.getStatus());
    }

    @Test
    void testProcessDrivingSummary_ItemAlreadyExists() {
        // Given
        DrivingSummaryV1 summary = createValidSummary();
        MilestoneReport report = createMockReport(1L, 10);

        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class)))
            .thenReturn(true);
        when(valueOperations.get(RedisKeys.activeReportForUser("123")))
            .thenReturn("1");
        when(reportRepo.findById(1L)).thenReturn(Optional.of(report));
        when(itemRepo.existsByReportIdAndDrivingId(1L, "driving-123"))
            .thenReturn(true);
        when(itemRepo.countByReportId(1L)).thenReturn(10);

        // When
        service.processDrivingSummary("msg-123", summary);

        // Then
        verify(itemRepo, never()).save(any());
        verify(producer, never()).emit(any());
    }

    @Test
    void testFindOrCreateActiveReport_FromCache() {
        // Given
        when(valueOperations.get(RedisKeys.activeReportForUser("123")))
            .thenReturn("1");
        MilestoneReport report = createMockReport(1L, 5);
        when(reportRepo.findById(1L)).thenReturn(Optional.of(report));

        // When
        DrivingSummaryV1 summary = createValidSummary();
        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class)))
            .thenReturn(true);
        when(itemRepo.existsByReportIdAndDrivingId(1L, "driving-123"))
            .thenReturn(false);
        when(itemRepo.countByReportId(1L)).thenReturn(5, 6);

        service.processDrivingSummary("msg-123", summary);

        // Then
        verify(reportRepo).findById(1L);
        verify(reportRepo, never()).findFirstByUserIdAndStatusOrderByIdDesc(any(), any());
    }

    @Test
    void testFindOrCreateActiveReport_CreateNew() {
        // Given
        when(valueOperations.get(RedisKeys.activeReportForUser("123")))
            .thenReturn(null);
        when(reportRepo.findFirstByUserIdAndStatusOrderByIdDesc(123L, MilestoneReport.Status.COLLECTING))
            .thenReturn(Optional.empty());
        when(reportRepo.findTopByUserIdOrderByCycleNoDesc(123L))
            .thenReturn(Optional.empty());
        
        MilestoneReport newReport = createMockReport(2L, 0);
        when(reportRepo.save(any(MilestoneReport.class))).thenReturn(newReport);

        // When
        DrivingSummaryV1 summary = createValidSummary();
        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class)))
            .thenReturn(true);
        when(itemRepo.existsByReportIdAndDrivingId(2L, "driving-123"))
            .thenReturn(false);
        when(itemRepo.countByReportId(2L)).thenReturn(0, 1);

        service.processDrivingSummary("msg-123", summary);

        // Then
        verify(reportRepo, times(2)).save(any(MilestoneReport.class)); // 한 번은 생성 시, 한 번은 item 추가 시
        verify(valueOperations).set(
            eq(RedisKeys.activeReportForUser("123")), 
            eq("2"), 
            eq(Duration.ofDays(30))
        );
    }

    private DrivingSummaryV1 createValidSummary() {
        DrivingSummaryV1 summary = new DrivingSummaryV1();
        summary.setUserId("123");
        summary.setDrivingId("driving-123");
        summary.setStatus("COMPLETED");
        summary.setEndedAt(System.currentTimeMillis());
        return summary;
    }

    private MilestoneReport createMockReport(Long id, int numberOfDriving) {
        MilestoneReport report = MilestoneReport.builder()
            .id(id)
            .userId(123L)
            .cycleNo(1)
            .numberOfDriving(numberOfDriving)
            .status(MilestoneReport.Status.COLLECTING)
            .read(false)
            .build();
        return report;
    }

    private List<MilestoneItem> createMockItems() {
        return List.of(
            MilestoneItem.builder().drivingId("driving-1").build(),
            MilestoneItem.builder().drivingId("driving-2").build(),
            MilestoneItem.builder().drivingId("driving-3").build()
        );
    }
}