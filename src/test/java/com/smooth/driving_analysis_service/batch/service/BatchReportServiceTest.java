package com.smooth.driving_analysis_service.batch.service;

import com.smooth.driving_analysis_service.batch.dto.ReportTriggerV1;
import com.smooth.driving_analysis_service.reports.dna.service.DnaBatchService;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchReportServiceTest {

    @Mock
    private MilestoneReportRepository milestoneReportRepository;

    @Mock
    private DnaBatchService dnaBatchService;

    @InjectMocks
    private BatchReportService batchReportService;

    @Test
    void testProcessReportTrigger_InterimTrigger() {
        // Given
        ReportTriggerV1 trigger = ReportTriggerV1.builder()
                .type("INTERIM")
                .userId("12345")
                .reportId(1L)
                .milestone(4)
                .status("COLLECTING")
                .drivingIds(Arrays.asList("trip-001", "trip-002", "trip-003", "trip-004"))
                .build();

        // When
        batchReportService.processReportTrigger(trigger);

        // Then
        verify(dnaBatchService).runInterim(1L);
        verify(milestoneReportRepository, never()).findById(any());
        verify(milestoneReportRepository, never()).save(any());
    }

    @Test
    void testProcessReportTrigger_FinalTrigger() {
        // Given
        ReportTriggerV1 trigger = ReportTriggerV1.builder()
                .type("FINAL")
                .userId("12345")
                .reportId(1L)
                .milestone(15)
                .status("PROCESSING")
                .drivingIds(Arrays.asList("trip-001", "trip-002", "trip-015"))
                .build();

        MilestoneReport report = MilestoneReport.builder()
                .id(1L)
                .userId(12345L)
                .status(MilestoneReport.Status.PROCESSING)
                .numberOfDriving(15)
                .build();

        when(milestoneReportRepository.findById(1L)).thenReturn(Optional.of(report));

        // When
        batchReportService.processReportTrigger(trigger);

        // Then
        verify(dnaBatchService).runFinal(1L);
        verify(milestoneReportRepository).findById(1L);
        verify(milestoneReportRepository).save(argThat(r -> 
            r.getStatus() == MilestoneReport.Status.COMPLETED
        ));
    }

    @Test
    void testProcessReportTrigger_UnknownType() {
        // Given
        ReportTriggerV1 trigger = ReportTriggerV1.builder()
                .type("UNKNOWN")
                .userId("12345")
                .reportId(1L)
                .milestone(4)
                .build();

        // When
        batchReportService.processReportTrigger(trigger);

        // Then
        verify(dnaBatchService, never()).runInterim(any());
        verify(dnaBatchService, never()).runFinal(any());
    }

    @Test
    void testProcessPendingReports() {
        // Given
        MilestoneReport report1 = MilestoneReport.builder()
                .id(1L)
                .userId(12345L)
                .status(MilestoneReport.Status.PROCESSING)
                .numberOfDriving(15)
                .build();

        MilestoneReport report2 = MilestoneReport.builder()
                .id(2L)
                .userId(67890L)
                .status(MilestoneReport.Status.PROCESSING)
                .numberOfDriving(15)
                .build();

        List<MilestoneReport> processingReports = Arrays.asList(report1, report2);
        when(milestoneReportRepository.findByStatus(MilestoneReport.Status.PROCESSING))
                .thenReturn(processingReports);
        
        // processFinalReport에서 findById 호출을 위한 Mock 설정 추가
        when(milestoneReportRepository.findById(1L)).thenReturn(Optional.of(report1));
        when(milestoneReportRepository.findById(2L)).thenReturn(Optional.of(report2));

        // When
        batchReportService.processPendingReports();

        // Then
        verify(milestoneReportRepository).findByStatus(MilestoneReport.Status.PROCESSING);
        verify(milestoneReportRepository, times(2)).findById(any());
        verify(milestoneReportRepository, times(2)).save(any());
        verify(dnaBatchService, times(2)).runFinal(any());
    }

    @Test
    void testProcessPendingReports_ExceptionHandling() {
        // Given
        MilestoneReport report = MilestoneReport.builder()
                .id(1L)
                .userId(12345L)
                .status(MilestoneReport.Status.PROCESSING)
                .numberOfDriving(15)
                .build();

        when(milestoneReportRepository.findByStatus(MilestoneReport.Status.PROCESSING))
                .thenReturn(Arrays.asList(report));
        doThrow(new RuntimeException("DNA processing failed")).when(dnaBatchService).runFinal(1L);

        // When
        batchReportService.processPendingReports();

        // Then
        verify(milestoneReportRepository).findByStatus(MilestoneReport.Status.PROCESSING);
        verify(dnaBatchService).runFinal(1L);
        // 예외가 발생해도 다른 리포트 처리는 계속되어야 함
    }
}