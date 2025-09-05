package com.smooth.driving_analysis_service.reports.dna.service;

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
    private DnaMetricSource dnaMetricSource;

    @Test
    void testLoadForReport() {
        // Given
        Long reportId = 1L;
        List<String> drivingIds = Arrays.asList("trip-001", "trip-002", "trip-003");

        // When
        DnaMetricSource.ReportMetrics result = dnaMetricSource.loadForReport(reportId, drivingIds);

        // Then
        assertNotNull(result);
        assertNotNull(result.getDrivings());
        assertEquals(3, result.getDrivings().size());

        // 첫 번째 driving 검증
        DnaMetricSource.PerDriving firstDriving = result.getDrivings().get(0);
        assertEquals("trip-001", firstDriving.getDrivingId());
        assertEquals(15.0, firstDriving.getDistanceKm());
        assertEquals(1.2, firstDriving.getLaneChangePerKm());
        assertEquals(0.8, firstDriving.getPostChangeAccel());
        assertEquals(6.5, firstDriving.getSec0to40());
        assertEquals(1.2, firstDriving.getAvgDecelRate());
    }

    @Test
    void testLoadForReport_EmptyDrivingIds() {
        // Given
        Long reportId = 1L;
        List<String> drivingIds = Arrays.asList();

        // When
        DnaMetricSource.ReportMetrics result = dnaMetricSource.loadForReport(reportId, drivingIds);

        // Then
        assertNotNull(result);
        assertNotNull(result.getDrivings());
        assertEquals(0, result.getDrivings().size());
    }

    @Test
    void testPerDriving_Builder() {
        // Given & When
        DnaMetricSource.PerDriving perDriving = DnaMetricSource.PerDriving.builder()
                .drivingId("test-trip")
                .distanceKm(20.0)
                .laneChangePerKm(1.5)
                .postChangeAccel(0.9)
                .sec0to40(5.5)
                .avgDecelRate(1.1)
                .build();

        // Then
        assertEquals("test-trip", perDriving.getDrivingId());
        assertEquals(20.0, perDriving.getDistanceKm());
        assertEquals(1.5, perDriving.getLaneChangePerKm());
        assertEquals(0.9, perDriving.getPostChangeAccel());
        assertEquals(5.5, perDriving.getSec0to40());
        assertEquals(1.1, perDriving.getAvgDecelRate());
    }

    @Test
    void testReportMetrics_Constructor() {
        // Given
        List<DnaMetricSource.PerDriving> drivings = Arrays.asList(
                DnaMetricSource.PerDriving.builder().drivingId("trip-001").build(),
                DnaMetricSource.PerDriving.builder().drivingId("trip-002").build()
        );

        // When
        DnaMetricSource.ReportMetrics reportMetrics = new DnaMetricSource.ReportMetrics(drivings);

        // Then
        assertNotNull(reportMetrics.getDrivings());
        assertEquals(2, reportMetrics.getDrivings().size());
        assertEquals("trip-001", reportMetrics.getDrivings().get(0).getDrivingId());
        assertEquals("trip-002", reportMetrics.getDrivings().get(1).getDrivingId());
    }
}