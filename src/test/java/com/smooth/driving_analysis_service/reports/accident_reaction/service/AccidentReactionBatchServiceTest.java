package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import com.smooth.driving_analysis_service.reports.accident_reaction.entity.AccidentReactionMetric;
import com.smooth.driving_analysis_service.reports.accident_reaction.repository.AccidentReactionMetricRepository;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneItem;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccidentReactionBatchServiceTest {

    @Mock
    private AccidentReactionMetricRepository accidentReactionMetricRepository;

    @Mock
    private MilestoneItemRepository milestoneItemRepository;

    @InjectMocks
    private AccidentReactionBatchService accidentReactionBatchService;

    @Test
    void testCreateOrUpdateInterimSnapshot_Success() {
        // Given
        Long reportId = 1L;
        List<MilestoneItem> items = Arrays.asList(
                createMilestoneItem(1L, "trip-001", 1),
                createMilestoneItem(1L, "trip-002", 2),
                createMilestoneItem(1L, "trip-003", 3),
                createMilestoneItem(1L, "trip-004", 4)
        );

        List<AccidentReactionMetric> metrics = Arrays.asList(
                createAccidentReactionMetric("alert-001", "trip-001"),
                createAccidentReactionMetric("alert-002", "trip-002")
        );

        when(milestoneItemRepository.findByReportIdOrderByOrderNoAsc(reportId)).thenReturn(items);
        when(accidentReactionMetricRepository.findByDrivingIdIn(anyList())).thenReturn(metrics);

        // When
        accidentReactionBatchService.createOrUpdateInterimSnapshot(reportId);

        // Then
        verify(milestoneItemRepository).findByReportIdOrderByOrderNoAsc(reportId);
        verify(accidentReactionMetricRepository).findByDrivingIdIn(Arrays.asList("trip-001", "trip-002", "trip-003", "trip-004"));
    }

    @Test
    void testCreateOrUpdateInterimSnapshot_EmptyDrivingIds() {
        // Given
        Long reportId = 1L;
        when(milestoneItemRepository.findByReportIdOrderByOrderNoAsc(reportId)).thenReturn(List.of());

        // When
        accidentReactionBatchService.createOrUpdateInterimSnapshot(reportId);

        // Then
        verify(milestoneItemRepository).findByReportIdOrderByOrderNoAsc(reportId);
        verify(accidentReactionMetricRepository, never()).findByDrivingIdIn(anyList());
    }

    @Test
    void testCreateFinalSnapshot_Success() {
        // Given
        Long reportId = 1L;
        List<MilestoneItem> items = Arrays.asList(
                createMilestoneItem(1L, "trip-001", 1),
                createMilestoneItem(1L, "trip-002", 2),
                createMilestoneItem(1L, "trip-015", 15)
        );

        List<AccidentReactionMetric> metrics = Arrays.asList(
                createAccidentReactionMetric("alert-001", "trip-001"),
                createAccidentReactionMetric("alert-002", "trip-015")
        );

        when(milestoneItemRepository.findByReportIdOrderByOrderNoAsc(reportId)).thenReturn(items);
        when(accidentReactionMetricRepository.findByDrivingIdIn(anyList())).thenReturn(metrics);

        // When
        accidentReactionBatchService.createFinalSnapshot(reportId);

        // Then
        verify(milestoneItemRepository).findByReportIdOrderByOrderNoAsc(reportId);
        verify(accidentReactionMetricRepository).findByDrivingIdIn(Arrays.asList("trip-001", "trip-002", "trip-015"));
    }

    @Test
    void testCreateFinalSnapshot_ExceptionHandling() {
        // Given
        Long reportId = 1L;
        when(milestoneItemRepository.findByReportIdOrderByOrderNoAsc(reportId))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        try {
            accidentReactionBatchService.createFinalSnapshot(reportId);
        } catch (RuntimeException e) {
            // Expected exception
        }

        verify(milestoneItemRepository).findByReportIdOrderByOrderNoAsc(reportId);
        verify(accidentReactionMetricRepository, never()).findByDrivingIdIn(anyList());
    }

    private MilestoneItem createMilestoneItem(Long reportId, String drivingId, int orderNo) {
        return MilestoneItem.builder()
                .reportId(reportId)
                .drivingId(drivingId)
                .orderNo(orderNo)
                .build();
    }

    private AccidentReactionMetric createAccidentReactionMetric(String alertId, String drivingId) {
        return AccidentReactionMetric.builder()
                .alertId(alertId)
                .userId(12345L)
                .drivingId(drivingId)
                .renderedAt(LocalDateTime.now())
                .reacted(true)
                .reactionMs(1500)
                .eventType("hard_brake")
                .decelOrStop(true)
                .evasiveManeuver(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}