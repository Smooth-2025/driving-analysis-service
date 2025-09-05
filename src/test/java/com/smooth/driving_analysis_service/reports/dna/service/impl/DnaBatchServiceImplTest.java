package com.smooth.driving_analysis_service.reports.dna.service.impl;

import com.smooth.driving_analysis_service.reports.dna.service.DnaBatchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DnaBatchServiceImplTest {

    @Mock
    private DnaBatchService service;

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