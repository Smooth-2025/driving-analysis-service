package com.smooth.driving_analysis_service.reports.dna.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DnaMetricSourceTest {

    @Mock
    private DnaMetricSource dnaMetricSource;

    @Test
    void testLoadForReport() {
        // Given
        Long reportId = 1L;
        List<String> drivingIds = Arrays.asList("trip-001", "trip-002", "trip-003");
        
        List<DnaMetricSource.PerDriving> mockDrivings = Arrays.asList(
            new DnaMetricSource.PerDriving("trip-001", 6.5, 1.2, 1.2, 0.8, 15.0),
            new DnaMetricSource.PerDriving("trip-002", 7.0, 1.0, 1.5, 0.9, 18.0),
            new DnaMetricSource.PerDriving("trip-003", 5.5, 1.5, 2.0, 1.1, 12.0)
        );
        DnaMetricSource.DnaInput mockResult = new DnaMetricSource.DnaInput(mockDrivings);
        
        when(dnaMetricSource.loadForReport(reportId, drivingIds)).thenReturn(mockResult);

        // When
        DnaMetricSource.DnaInput result = dnaMetricSource.loadForReport(reportId, drivingIds);

        // Then
        assertNotNull(result);
        assertNotNull(result.drivings());
        assertEquals(3, result.drivings().size());

        // 첫 번째 driving 검증
        DnaMetricSource.PerDriving firstDriving = result.drivings().get(0);
        assertEquals("trip-001", firstDriving.drivingId());
        assertEquals(15.0, firstDriving.distanceKm());
        assertEquals(1.2, firstDriving.laneChangePerKm());
        assertEquals(0.8, firstDriving.postChangeAccel());
        assertEquals(6.5, firstDriving.sec0to40());
        assertEquals(1.2, firstDriving.avgDecelRate());
    }

    @Test
    void testLoadForReport_EmptyDrivingIds() {
        // Given
        Long reportId = 1L;
        List<String> drivingIds = Arrays.asList();
        DnaMetricSource.DnaInput mockResult = new DnaMetricSource.DnaInput(Arrays.asList());
        
        when(dnaMetricSource.loadForReport(reportId, drivingIds)).thenReturn(mockResult);

        // When
        DnaMetricSource.DnaInput result = dnaMetricSource.loadForReport(reportId, drivingIds);

        // Then
        assertNotNull(result);
        assertNotNull(result.drivings());
        assertEquals(0, result.drivings().size());
    }

    @Test
    void testPerDriving_Record() {
        // Given & When
        DnaMetricSource.PerDriving perDriving = new DnaMetricSource.PerDriving(
            "test-trip", 5.5, 1.1, 1.5, 0.9, 20.0
        );

        // Then
        assertEquals("test-trip", perDriving.drivingId());
        assertEquals(20.0, perDriving.distanceKm());
        assertEquals(1.5, perDriving.laneChangePerKm());
        assertEquals(0.9, perDriving.postChangeAccel());
        assertEquals(5.5, perDriving.sec0to40());
        assertEquals(1.1, perDriving.avgDecelRate());
    }

    @Test
    void testDnaInput_Constructor() {
        // Given
        List<DnaMetricSource.PerDriving> drivings = Arrays.asList(
            new DnaMetricSource.PerDriving("trip-001", 6.0, 1.0, 1.0, 0.5, 10.0),
            new DnaMetricSource.PerDriving("trip-002", 7.0, 1.2, 1.5, 0.8, 15.0)
        );

        // When
        DnaMetricSource.DnaInput dnaInput = new DnaMetricSource.DnaInput(drivings);

        // Then
        assertNotNull(dnaInput.drivings());
        assertEquals(2, dnaInput.drivings().size());
        assertEquals("trip-001", dnaInput.drivings().get(0).drivingId());
        assertEquals("trip-002", dnaInput.drivings().get(1).drivingId());
    }
}