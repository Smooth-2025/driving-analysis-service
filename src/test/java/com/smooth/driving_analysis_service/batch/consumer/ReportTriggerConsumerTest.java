package com.smooth.driving_analysis_service.batch.consumer;

import com.smooth.driving_analysis_service.batch.dto.ReportTriggerV1;
import com.smooth.driving_analysis_service.batch.service.BatchReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportTriggerConsumerTest {

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private StreamOperations<String, Object, Object> streamOperations;

    @Mock
    private BatchReportService batchReportService;

    @InjectMocks
    private ReportTriggerConsumer reportTriggerConsumer;

    @Test
    void testOnMessage_InterimTrigger() {
        // Given
        Map<String, String> messageData = Map.of(
                "v", "1",
                "type", "INTERIM",
                "userId", "12345",
                "reportId", "1",
                "milestone", "4",
                "status", "COLLECTING",
                "drivingIds", "trip-001,trip-002,trip-003,trip-004"
        );

        RecordId recordId = RecordId.of("1234567890-0");
        MapRecord<String, String, String> message = MapRecord.create("report-trigger", messageData);
        message = message.withId(recordId);

        when(redis.opsForStream()).thenReturn(streamOperations);

        // When
        reportTriggerConsumer.onMessage(message);

        // Then
        verify(batchReportService).processReportTrigger(any(ReportTriggerV1.class));
    }

    @Test
    void testOnMessage_FinalTrigger() {
        // Given
        Map<String, String> messageData = Map.of(
                "v", "1",
                "type", "FINAL",
                "userId", "12345",
                "reportId", "1",
                "milestone", "15",
                "status", "PROCESSING",
                "drivingIds", "trip-001,trip-002,trip-003,trip-004,trip-005,trip-006,trip-007,trip-008,trip-009,trip-010,trip-011,trip-012,trip-013,trip-014,trip-015"
        );

        RecordId recordId = RecordId.of("1234567890-1");
        MapRecord<String, String, String> message = MapRecord.create("report-trigger", messageData);
        message = message.withId(recordId);

        when(redis.opsForStream()).thenReturn(streamOperations);

        // When
        reportTriggerConsumer.onMessage(message);

        // Then
        verify(batchReportService).processReportTrigger(any(ReportTriggerV1.class));
    }

    @Test
    void testOnMessage_ExceptionHandling() {
        // Given
        Map<String, String> messageData = Map.of(
                "v", "1",
                "type", "INTERIM",
                "userId", "12345",
                "reportId", "1",
                "milestone", "4",
                "status", "COLLECTING",
                "drivingIds", "trip-001,trip-002"
        );

        RecordId recordId = RecordId.of("1234567890-2");
        MapRecord<String, String, String> message = MapRecord.create("report-trigger", messageData);
        message = message.withId(recordId);

        doThrow(new RuntimeException("Processing failed")).when(batchReportService).processReportTrigger(any(ReportTriggerV1.class));

        // When
        reportTriggerConsumer.onMessage(message);

        // Then
        verify(batchReportService).processReportTrigger(any(ReportTriggerV1.class));
        // 예외가 발생하면 ACK는 처리되지 않음
        verify(redis, never()).opsForStream();
    }
}