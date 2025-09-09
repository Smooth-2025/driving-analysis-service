package com.smooth.driving_analysis_service.trigger.service;

import com.smooth.driving_analysis_service.global.redis.RedisKeys;
import com.smooth.driving_analysis_service.reports.pipeline.service.DrivingIntegrationService;
import com.smooth.driving_analysis_service.reports.milestone.service.MilestoneService;
import com.smooth.driving_analysis_service.reports.trigger.dto.DrivingSummaryV1;
import com.smooth.driving_analysis_service.reports.trigger.service.DrivingSummaryConsumerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DrivingSummaryConsumerService 테스트")
class DrivingSummaryConsumerServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private DrivingIntegrationService drivingIntegrationService;

    @Mock
    private MilestoneService milestoneService;

    @InjectMocks
    private DrivingSummaryConsumerService service;

    @Test
    @DisplayName("완료되지 않은 주행은 처리하지 않음")
    void testProcessDrivingSummary_NotCompleted() {
        // Given
        DrivingSummaryV1 summary = new DrivingSummaryV1();
        summary.setUserId("123");
        summary.setDrivingId("driving-123");
        summary.setEndedAt("1234567890");
        summary.setStatus("PROCESSING");

        // When & Then
        assertThatThrownBy(() -> service.processDrivingSummary("msg-123", summary))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only COMPLETED trips can be processed");

        verifyNoInteractions(redisTemplate, drivingIntegrationService, milestoneService);
    }

    @Test
    @DisplayName("이미 처리된 주행은 스킵")
    void testProcessDrivingSummary_AlreadyProcessed() {
        // Given
        DrivingSummaryV1 summary = createValidSummary();
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class)))
            .thenReturn(false);

        // When
        service.processDrivingSummary("msg-123", summary);

        // Then
        verify(valueOperations).setIfAbsent(
            eq(RedisKeys.processedTrip("trip-001")), 
            eq("1"), 
            eq(Duration.ofDays(7))
        );
        verifyNoInteractions(drivingIntegrationService, milestoneService);
    }

    @Test
    @DisplayName("새로운 주행 처리 성공")
    void testProcessDrivingSummary_Success() {
        // Given
        DrivingSummaryV1 summary = createValidSummary();
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class)))
            .thenReturn(true);

        // When
        service.processDrivingSummary("msg-123", summary);

        // Then
        verify(valueOperations).setIfAbsent(
            eq(RedisKeys.processedTrip("trip-001")), 
            eq("1"), 
            eq(Duration.ofDays(7))
        );
        verify(drivingIntegrationService).integrateAndSave(summary);
        verify(milestoneService).processDrivingCompleted(12345L, "trip-001");
    }

    @Test
    @DisplayName("처리 중 예외 발생 시 멱등성 키 삭제")
    void testProcessDrivingSummary_ExceptionHandling() {
        // Given
        DrivingSummaryV1 summary = createValidSummary();
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class)))
            .thenReturn(true);
        doThrow(new RuntimeException("Integration failed"))
            .when(drivingIntegrationService).integrateAndSave(summary);

        // When & Then
        assertThatThrownBy(() -> service.processDrivingSummary("msg-123", summary))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Integration failed");

        verify(redisTemplate).delete(RedisKeys.processedTrip("trip-001"));
    }

    @Test
    @DisplayName("메시지 ID 없이 처리 (기존 호환성)")
    void testHandle() {
        // Given
        DrivingSummaryV1 summary = createValidSummary();
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class)))
            .thenReturn(true);

        // When
        service.handle(summary);

        // Then
        verify(drivingIntegrationService).integrateAndSave(summary);
        verify(milestoneService).processDrivingCompleted(12345L, "trip-001");
    }

    @Test
    @DisplayName("유효하지 않은 데이터 처리 시 예외 발생")
    void testProcessDrivingSummary_InvalidData() {
        // Given
        DrivingSummaryV1 summary = new DrivingSummaryV1();
        summary.setUserId("12345");
        summary.setDrivingId("trip-001");
        // endedAt, status 누락

        // When & Then
        assertThatThrownBy(() -> service.processDrivingSummary("msg-123", summary))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Missing required fields");

        verifyNoInteractions(redisTemplate, drivingIntegrationService, milestoneService);
    }

    private DrivingSummaryV1 createValidSummary() {
        DrivingSummaryV1 summary = new DrivingSummaryV1();
        summary.setV(1);
        summary.setUserId("12345");
        summary.setDrivingId("trip-001");
        summary.setStartedAt("2025-01-09T10:00:00");
        summary.setEndedAt("2025-01-09T10:30:00");
        summary.setStatus("COMPLETED");
        summary.setProducer("test");
        summary.setDrivingMinutes(30);
        summary.setTotalDistance(15000);
        summary.setLaneChangeCount(3);
        summary.setHardBrakeCount(1);
        summary.setRapidAccelCount(2);
        return summary;
    }
}