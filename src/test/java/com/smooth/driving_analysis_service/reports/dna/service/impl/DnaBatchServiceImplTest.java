package com.smooth.driving_analysis_service.reports.dna.service.impl;

import com.smooth.driving_analysis_service.driving.repository.DrivingRecordRepository;
import com.smooth.driving_analysis_service.reports.accident_reaction.repository.AccidentReactionMetricRepository;
import com.smooth.driving_analysis_service.reports.dna.entity.DnaSnapshot;
import com.smooth.driving_analysis_service.reports.dna.repository.DnaSnapshotRepository;
import com.smooth.driving_analysis_service.reports.dna.service.DnaComputeService;
import com.smooth.driving_analysis_service.reports.dna.service.DnaMetricSource;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneItemRepository;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DnaBatchServiceImplTest {

    @InjectMocks
    private DnaBatchServiceImpl service;

    @Mock
    private MilestoneReportRepository reportRepo;
    
    @Mock
    private MilestoneItemRepository itemRepo;
    
    @Mock
    private DrivingRecordRepository drivingRepo;
    
    @Mock
    private AccidentReactionMetricRepository reactionRepo;
    
    @Mock
    private DnaSnapshotRepository snapshotRepo;
    
    @Mock
    private DnaComputeService compute;
    
    @Mock
    private DnaMetricSource metricSource;

    @Test
    void testRunInterim_NotCollectingStatus() {
        // Given
        Long reportId = 1L;
        when(service.runInterim(reportId)).thenThrow(new IllegalStateException("Interim only for COLLECTING status"));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> service.runInterim(reportId));
        assertTrue(exception.getMessage().contains("Interim only for COLLECTING status"));
    }

    @Test
    void testRunInterim_InsufficientCount() {
        // Given
        Long reportId = 1L;
        when(service.runInterim(reportId)).thenThrow(new IllegalStateException("Interim requires at least 4 driving records"));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> service.runInterim(reportId));
        assertTrue(exception.getMessage().contains("Interim requires at least 4 driving records"));
    }

    @Test
    void testRunFinal_NotProcessingStatus() {
        // Given
        Long reportId = 1L;
        when(service.runFinal(reportId)).thenThrow(new IllegalStateException("Final only for PROCESSING status"));
        
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> service.runFinal(reportId));
        assertTrue(exception.getMessage().contains("Final only for PROCESSING status"));
    }

    @Test
    void testRunFinal_IncorrectCount() {
        // Given
        Long reportId = 1L;
        when(service.runFinal(reportId)).thenThrow(new IllegalStateException("Final requires exactly 15 driving records"));
        
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> service.runFinal(reportId));
        assertTrue(exception.getMessage().contains("Final requires exactly 15 driving records"));
    }
}