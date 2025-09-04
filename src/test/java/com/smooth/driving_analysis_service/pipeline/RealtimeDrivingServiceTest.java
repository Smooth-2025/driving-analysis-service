package com.smooth.driving_analysis_service.pipeline;

import com.smooth.driving_analysis_service.driving.entity.DrivingRecord;
import com.smooth.driving_analysis_service.driving.repository.DrivingRecordRepository;
import com.smooth.driving_analysis_service.pipeline.entity.DrivingAccumulatedStats;
import com.smooth.driving_analysis_service.pipeline.entity.DrivingEventAgg;
import com.smooth.driving_analysis_service.pipeline.entity.DrivingTimeBin;
import com.smooth.driving_analysis_service.pipeline.repository.DrivingAccumulatedStatsRepository;
import com.smooth.driving_analysis_service.pipeline.repository.DrivingEventAggRepository;
import com.smooth.driving_analysis_service.pipeline.repository.DrivingTimeBinRepository;
import com.smooth.driving_analysis_service.trigger.dto.DrivingSummaryV1;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RealtimeDrivingServiceTest {

    @Mock
    private DrivingRecordRepository drivingRepo;

    @Mock
    private DrivingEventAggRepository eventAggRepo;

    @Mock
    private DrivingTimeBinRepository timeBinRepo;

    @Mock
    private DrivingAccumulatedStatsRepository accumulatedStatsRepo;

    @InjectMocks
    private RealtimeDrivingService service;

    @Test
    void testApplySummary_NewRecord() {
        // Given
        DrivingSummaryV1 summary = createValidSummary();
        
        when(drivingRepo.findByDrivingId("driving-123")).thenReturn(Optional.empty());
        when(eventAggRepo.findById("driving-123")).thenReturn(Optional.empty());
        when(accumulatedStatsRepo.findByDrivingId("driving-123")).thenReturn(Optional.empty());

        DrivingRecord mockRecord = DrivingRecord.createInitialRecord("driving-123", 123L);
        when(drivingRepo.save(any(DrivingRecord.class))).thenReturn(mockRecord);

        // When
        service.applySummary(summary);

        // Then
        verify(drivingRepo).save(any(DrivingRecord.class));
        verify(eventAggRepo).save(any(DrivingEventAgg.class));
        verify(accumulatedStatsRepo).save(any(DrivingAccumulatedStats.class));
        verify(timeBinRepo).deleteByDrivingId("driving-123");
        verify(timeBinRepo, atLeastOnce()).save(any(DrivingTimeBin.class));
    }

    @Test
    void testApplySummary_ExistingRecord() {
        // Given
        DrivingSummaryV1 summary = createValidSummary();
        
        DrivingRecord existingRecord = DrivingRecord.builder()
                .id(1L)
                .drivingId("driving-123")
                .userId(123L)
                .build();
        
        DrivingEventAgg existingAgg = DrivingEventAgg.builder()
                .drivingId("driving-123")
                .userId(123L)
                .laneChangeCount(2)
                .hardBrakeCount(1)
                .rapidAccelCount(1)
                .sharpTurnCount(0)
                .build();

        DrivingAccumulatedStats existingStats = DrivingAccumulatedStats.builder()
                .id(1L)
                .userId(123L)
                .drivingId("driving-123")
                .build();

        when(drivingRepo.findByDrivingId("driving-123")).thenReturn(Optional.of(existingRecord));
        when(eventAggRepo.findById("driving-123")).thenReturn(Optional.of(existingAgg));
        when(accumulatedStatsRepo.findByDrivingId("driving-123")).thenReturn(Optional.of(existingStats));

        // When
        service.applySummary(summary);

        // Then
        ArgumentCaptor<DrivingAccumulatedStats> statsCaptor = ArgumentCaptor.forClass(DrivingAccumulatedStats.class);
        verify(accumulatedStatsRepo).save(statsCaptor.capture());
        
        DrivingAccumulatedStats savedStats = statsCaptor.getValue();
        assertEquals(30, savedStats.getDrivingMinutes());
        assertEquals(15000, savedStats.getTotalDistance());
        assertEquals(3, savedStats.getLaneChangeCount());
        assertEquals(1, savedStats.getHardBrakeCount());
        assertEquals(2, savedStats.getRapidAccelCount());
        assertNotNull(savedStats.getStartTime());
        assertNotNull(savedStats.getEndTime());
    }

    @Test
    void testApplySummary_CalculatesAvgSpeed() {
        // Given
        DrivingSummaryV1 summary = createValidSummary();
        
        when(drivingRepo.findByDrivingId("driving-123")).thenReturn(Optional.empty());
        when(eventAggRepo.findById("driving-123")).thenReturn(Optional.empty());
        when(accumulatedStatsRepo.findByDrivingId("driving-123")).thenReturn(Optional.empty());

        DrivingRecord mockRecord = DrivingRecord.createInitialRecord("driving-123", 123L);
        when(drivingRepo.save(any(DrivingRecord.class))).thenReturn(mockRecord);

        // When
        service.applySummary(summary);

        // Then
        ArgumentCaptor<DrivingRecord> recordCaptor = ArgumentCaptor.forClass(DrivingRecord.class);
        verify(drivingRepo).save(recordCaptor.capture());
        
        DrivingRecord savedRecord = recordCaptor.getValue();
        assertEquals(15.0, savedRecord.getTotalDistance()); // 15000m -> 15km
        assertEquals(30.0, savedRecord.getAvgSpeed()); // 15km / 0.5h = 30km/h
    }

    private DrivingSummaryV1 createValidSummary() {
        DrivingSummaryV1 summary = new DrivingSummaryV1();
        summary.setUserId("123");
        summary.setDrivingId("driving-123");
        summary.setStatus("COMPLETED");
        summary.setDrivingMinutes(30);
        summary.setTotalDistance(15000.0); // Double 타입
        summary.setLaneChangeCount(3);
        summary.setHardBrakeCount(1);
        summary.setRapidAccelCount(2);
        // Long 타입 timestamp 사용
        long now = System.currentTimeMillis();
        summary.setStartedAt(now - 30 * 60 * 1000); // 30분 전
        summary.setEndedAt(now);
        return summary;
    }
}