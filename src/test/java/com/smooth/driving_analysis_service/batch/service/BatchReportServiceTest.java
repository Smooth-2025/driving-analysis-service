package com.smooth.driving_analysis_service.batch.service;

import com.smooth.driving_analysis_service.batch.dto.ReportTriggerV1;
import com.smooth.driving_analysis_service.reports.basic_summary.service.BasicSummaryService;
import com.smooth.driving_analysis_service.reports.behavior.service.BehaviorReportService;
import com.smooth.driving_analysis_service.reports.milestone.service.MilestoneService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BatchReportService 테스트")
class BatchReportServiceTest {

    @Mock
    private BasicSummaryService basicSummaryService;

    @Mock
    private BehaviorReportService behaviorReportService;

    @Mock
    private MilestoneService milestoneService;

    @InjectMocks
    private BatchReportServiceImpl batchReportService;

    @Test
    @DisplayName("INTERIM 트리거 처리 - 중간 분석 리포트 생성")
    void testProcessReportTrigger_InterimTrigger() {
        // Given
        ReportTriggerV1 trigger = ReportTriggerV1.builder()
                .v(1)
                .type("INTERIM")
                .userId("12345")
                .reportId(1L)
                .milestone(4)
                .status("COLLECTING")
                .drivingIds(Arrays.asList("trip-001", "trip-002", "trip-003", "trip-004"))
                .emittedAt(LocalDateTime.now())
                .producer("milestone-service")
                .traceId("trace-123")
                .build();

        // When
        batchReportService.processReportTrigger(trigger);

        // Then
        verify(basicSummaryService).generateInterimReport(1L, 12345L);
        // TODO: 다른 리포트 서비스들도 활성화되면 추가
        // verify(behaviorReportService).generateInterimReport(1L, 12345L, trigger.getDrivingIds());
        
        // 마일스톤 상태 변경은 INTERIM에서는 호출되지 않음
        verify(milestoneService, never()).markReportCompleted(any());
    }

    @Test
    @DisplayName("FINAL 트리거 처리 - 최종 분석 리포트 생성 및 상태 완료")
    void testProcessReportTrigger_FinalTrigger() {
        // Given
        ReportTriggerV1 trigger = ReportTriggerV1.builder()
                .v(1)
                .type("FINAL")
                .userId("12345")
                .reportId(1L)
                .milestone(15)
                .status("PROCESSING")
                .drivingIds(Arrays.asList("trip-001", "trip-002", "trip-015"))
                .emittedAt(LocalDateTime.now())
                .producer("milestone-service")
                .traceId("trace-123")
                .build();

        // When
        batchReportService.processReportTrigger(trigger);

        // Then
        verify(basicSummaryService).generateFinalReport(1L, 12345L);
        // TODO: 다른 리포트 서비스들도 활성화되면 추가
        // verify(behaviorReportService).generateFinalReport(1L, 12345L, trigger.getDrivingIds());
        
        // 마일스톤 상태를 COMPLETED로 변경
        verify(milestoneService).markReportCompleted(1L);
    }

    @Test
    @DisplayName("알 수 없는 트리거 타입 처리")
    void testProcessReportTrigger_UnknownType() {
        // Given
        ReportTriggerV1 trigger = ReportTriggerV1.builder()
                .v(1)
                .type("UNKNOWN")
                .userId("12345")
                .reportId(1L)
                .milestone(4)
                .build();

        // When
        batchReportService.processReportTrigger(trigger);

        // Then
        verifyNoInteractions(basicSummaryService, behaviorReportService, milestoneService);
    }

    @Test
    @DisplayName("INTERIM 처리 중 예외 발생 시 예외 전파")
    void testProcessReportTrigger_InterimException() {
        // Given
        ReportTriggerV1 trigger = ReportTriggerV1.builder()
                .type("INTERIM")
                .userId("12345")
                .reportId(1L)
                .milestone(4)
                .build();

        doThrow(new RuntimeException("Basic summary generation failed"))
                .when(basicSummaryService).generateInterimReport(1L, 12345L);

        // When & Then
        assertThatThrownBy(() -> batchReportService.processReportTrigger(trigger))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Basic summary generation failed");
    }

    @Test
    @DisplayName("FINAL 처리 중 예외 발생 시 예외 전파")
    void testProcessReportTrigger_FinalException() {
        // Given
        ReportTriggerV1 trigger = ReportTriggerV1.builder()
                .type("FINAL")
                .userId("12345")
                .reportId(1L)
                .milestone(15)
                .build();

        doThrow(new RuntimeException("Final report generation failed"))
                .when(basicSummaryService).generateFinalReport(1L, 12345L);

        // When & Then
        assertThatThrownBy(() -> batchReportService.processReportTrigger(trigger))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Final report generation failed");
        
        // 예외 발생 시 마일스톤 상태 변경은 호출되지 않음
        verify(milestoneService, never()).markReportCompleted(any());
    }

    @Test
    @DisplayName("스케줄링된 배치 처리 - 정상 실행")
    void testProcessPendingReports() {
        // When
        batchReportService.processPendingReports();

        // Then
        // 현재는 로그만 출력하고 실제 처리는 없음
        // TODO: 실패한 트리거 재처리 로직이 구현되면 테스트 추가
    }

    @Test
    @DisplayName("8회 마일스톤 INTERIM 트리거 처리")
    void testProcessReportTrigger_Milestone8() {
        // Given
        ReportTriggerV1 trigger = ReportTriggerV1.builder()
                .type("INTERIM")
                .userId("12345")
                .reportId(1L)
                .milestone(8)
                .status("COLLECTING")
                .drivingIds(Arrays.asList("trip-001", "trip-002", "trip-008"))
                .build();

        // When
        batchReportService.processReportTrigger(trigger);

        // Then
        verify(basicSummaryService).generateInterimReport(1L, 12345L);
        verify(milestoneService, never()).markReportCompleted(any());
    }

    @Test
    @DisplayName("12회 마일스톤 INTERIM 트리거 처리")
    void testProcessReportTrigger_Milestone12() {
        // Given
        ReportTriggerV1 trigger = ReportTriggerV1.builder()
                .type("INTERIM")
                .userId("12345")
                .reportId(1L)
                .milestone(12)
                .status("COLLECTING")
                .drivingIds(Arrays.asList("trip-001", "trip-002", "trip-012"))
                .build();

        // When
        batchReportService.processReportTrigger(trigger);

        // Then
        verify(basicSummaryService).generateInterimReport(1L, 12345L);
        verify(milestoneService, never()).markReportCompleted(any());
    }
}