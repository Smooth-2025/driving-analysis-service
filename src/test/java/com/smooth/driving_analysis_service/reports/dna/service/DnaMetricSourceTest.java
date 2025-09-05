package com.smooth.driving_analysis_service.reports.dna.service;

import com.smooth.driving_analysis_service.reports.dna.service.impl.TestDnaMetricSourceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DnaMetricSourceTest {

    @InjectMocks
    private TestDnaMetricSourceImpl dnaMetricSource;

    @Test
    void testLoadForReport() {
        // Given
        Long reportId = 1L;
        List<String> drivingIds = Arrays.asList("trip-001", "trip-002", "trip-003");

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
        assertEquals(1.0, firstDriving.laneChangePerKm());
        assertEquals(0.2, firstDriving.postChangeAccel());
        assertEquals(7.0, firstDriving.sec0to40());
        assertEquals(1.3, firstDriving.avgDecelRate());
    }

    @Test
    void testLoadForReport_EmptyDrivingIds() {
        // Given
        Long reportId = 1L;
        List<String> drivingIds = Arrays.asList();

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
                "test-trip",
                5.5,    // sec0to40
                1.1,    // avgDecelRate
                1.5,    // laneChangePerKm
                0.9,    // postChangeAccel
                20.0    // distanceKm
        );

        // Then
        assertEquals("test-trip", perDriving.drivingId());
        assertEquals(5.5, perDriving.sec0to40());
        assertEquals(1.1, perDriving.avgDecelRate());
        assertEquals(1.5, perDriving.laneChangePerKm());
        assertEquals(0.9, perDriving.postChangeAccel());
        assertEquals(20.0, perDriving.distanceKm());
    }

    @Test
    void testDnaInput_Constructor() {
        // Given
        List<DnaMetricSource.PerDriving> drivings = Arrays.asList(
                new DnaMetricSource.PerDriving("trip-001", 7.0, 1.3, 1.0, 0.2, 15.0),
                new DnaMetricSource.PerDriving("trip-002", 9.0, 1.6, 1.5, 0.3, 20.0)
        );

        // When
        DnaMetricSource.DnaInput dnaInput = new DnaMetricSource.DnaInput(drivings);

        // Then
        assertNotNull(dnaInput.drivings());
        assertEquals(2, dnaInput.drivings().size());
        assertEquals("trip-001", dnaInput.drivings().get(0).drivingId());
        assertEquals("trip-002", dnaInput.drivings().get(1).drivingId());
    }

    @Test
    void testVariousPatterns() {
        // Given
        Long reportId = 1L;
        List<String> drivingIds = Arrays.asList("trip-001", "trip-002", "trip-003", "trip-004", "trip-005");

        // When
        DnaMetricSource.DnaInput result = dnaMetricSource.loadForReport(reportId, drivingIds);

        // Then
        assertEquals(5, result.drivings().size());
        
        // 패턴 검증 (TestDnaMetricSourceImpl의 로직에 따라)
        for (int i = 0; i < result.drivings().size(); i++) {
            DnaMetricSource.PerDriving driving = result.drivings().get(i);
            assertNotNull(driving.drivingId());
            assertTrue(driving.sec0to40() >= 7.0 && driving.sec0to40() <= 11.0);
            assertTrue(driving.avgDecelRate() >= 1.3 && driving.avgDecelRate() <= 1.9);
            assertTrue(driving.laneChangePerKm() >= 1.0 && driving.laneChangePerKm() <= 2.0);
            assertTrue(driving.postChangeAccel() >= 0.2 && driving.postChangeAccel() <= 0.4);
            assertTrue(driving.distanceKm() >= 15.0 && driving.distanceKm() <= 25.0);
        }
    }
}