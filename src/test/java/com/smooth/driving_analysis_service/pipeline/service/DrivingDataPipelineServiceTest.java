package com.smooth.driving_analysis_service.pipeline.service;

import com.smooth.driving_analysis_service.driving.entity.DrivingRecord;
import com.smooth.driving_analysis_service.driving.repository.DrivingRecordRepository;
import com.smooth.driving_analysis_service.reports.basic_summary.entity.DrivingAccumulatedStats;
import com.smooth.driving_analysis_service.reports.basic_summary.repository.DrivingAccumulatedStatsRepository;
import com.smooth.driving_analysis_service.trigger.dto.DrivingSummaryV1;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DrivingDataPipelineServiceTest {

    @Mock
    private DrivingRecordRepository drivingRecordRepository;

    @Mock
    private DrivingAccumulatedStatsRepository drivingAccumulatedStatsRepository;

    @InjectMocks
    private DrivingDataPipelineService pipelineService;

    @Test
    void testIntegrateAndStore_WithDrivingRecord() {
        // Given
        DrivingSummaryV1 xaddData = createDrivingSummaryV1();
        DrivingRecord drivingRecord = createDrivingRecord();

        when(drivingRecordRepository.findByDrivingId("trip-001")).thenReturn(Optional.of(drivingRecord));

        // When
        pipelineService.integrateAndStore(xaddData);

        // Then
        verify(drivingRecordRepository).findByDrivingId("trip-001");
        verify(drivingAccumulatedStatsRepository).save(argThat(stats -> 
            stats.getDrivingId().equals("trip-001") &&
            stats.getUserId().equals(12345L) &&
            stats.getDrivingMinutes().equals(30) &&
            stats.getTotalDistance().equals(15000) &&
            stats.getAvgSpeed().equals(30.0) &&
            stats.getCruiseRatio().equals(0.75)
        ));
    }

    @Test
    void testIntegrateAndStore_WithoutDrivingRecord() {
        // Given
        DrivingSummaryV1 xaddData = createDrivingSummaryV1();

        when(drivingRecordRepository.findByDrivingId("trip-001")).thenReturn(Optional.empty());

        // When
        pipelineService.integrateAndStore(xaddData);

        // Then
        verify(drivingRecordRepository).findByDrivingId("trip-001");
        verify(drivingAccumulatedStatsRepository).save(argThat(stats -> 
            stats.getDrivingId().equals("trip-001") &&
            stats.getUserId().equals(12345L) &&
            stats.getDrivingMinutes().equals(30) &&
            stats.getTotalDistance().equals(15000) &&
            stats.getAvgSpeed() == null &&
            stats.getCruiseRatio() == null
        ));
    }

    @Test
    void testIntegrateAndStore_ExceptionHandling() {
        // Given
        DrivingSummaryV1 xaddData = createDrivingSummaryV1();

        when(drivingRecordRepository.findByDrivingId("trip-001"))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        try {
            pipelineService.integrateAndStore(xaddData);
        } catch (RuntimeException e) {
            // Expected exception
        }

        verify(drivingRecordRepository).findByDrivingId("trip-001");
        verify(drivingAccumulatedStatsRepository, never()).save(any());
    }

    @Test
    void testIntegrateAndStore_InvalidNumbers() {
        // Given
        DrivingSummaryV1 xaddData = DrivingSummaryV1.builder()
                .userId("12345")
                .drivingId("trip-001")
                .startedAt(1736416800000L) // 2025-01-09T10:00:00 in milliseconds
                .endedAt(1736418600000L)   // 2025-01-09T10:30:00 in milliseconds
                .status("COMPLETED")
                .drivingMinutes(null) // Invalid numbers should be null
                .totalDistance(null)  // Invalid numbers should be null
                .laneChangeCount(3)
                .hardBrakeCount(1)
                .rapidAccelCount(2)
                .build();

        when(drivingRecordRepository.findByDrivingId("trip-001")).thenReturn(Optional.empty());

        // When
        pipelineService.integrateAndStore(xaddData);

        // Then
        verify(drivingAccumulatedStatsRepository).save(argThat(stats -> 
            stats.getDrivingId().equals("trip-001") &&
            stats.getDrivingMinutes() == null &&
            stats.getTotalDistance() == null &&
            stats.getLaneChangeCount().equals(3)
        ));
    }

    private DrivingSummaryV1 createDrivingSummaryV1() {
        return DrivingSummaryV1.builder()
                .userId("12345")
                .drivingId("trip-001")
                .startedAt(1736416800000L) // 2025-01-09T10:00:00 in milliseconds
                .endedAt(1736418600000L)   // 2025-01-09T10:30:00 in milliseconds
                .status("COMPLETED")
                .drivingMinutes(30)
                .totalDistance(15000)
                .laneChangeCount(3)
                .hardBrakeCount(1)
                .rapidAccelCount(2)
                .build();
    }

    private DrivingRecord createDrivingRecord() {
        return DrivingRecord.builder()
                .drivingId("trip-001")
                .userId(12345L)
                .avgSpeed(30.0)
                .cruiseRatio(0.75)
                .startTime(LocalDateTime.of(2025, 1, 9, 10, 0, 0))
                .endTime(LocalDateTime.of(2025, 1, 9, 10, 30, 0))
                .build();
    }
}