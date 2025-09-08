package com.smooth.driving_analysis_service.batch.consumer;

import com.smooth.driving_analysis_service.batch.dto.ReportTriggerV1;
import com.smooth.driving_analysis_service.batch.service.BatchReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportTriggerConsumer 테스트")
class ReportTriggerConsumerTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private StreamOperations<String, Object, Object> streamOperations;

    @Mock
    private BatchReportService batchReportService;

    @InjectMocks
    private ReportTriggerConsumer consumer;

    @Test
    @DisplayName("INTERIM 트리거 메시지 처리")
    void testOnMessage_InterimTrigger() {
        // Given
        ReflectionTestUtils.setField(consumer, "streamKey", "report.trigger");
        ReflectionTestUtils.setField(consumer, "groupName", "batch-report-group");
        
        Map<String, String> messageData = Map.of(
                "v", "1",
                "type", "INTERIM",
                "userId", "12345",
                "reportId", "1",
                "milestone", "4",
                "status", "COLLECTING",
                "drivingIds", "trip-001,trip-002,trip-003,trip-004",
                "emittedAt", "2025-01-09T10:30:00",
                "producer", "milestone-service",
                "traceId", "trace-123"
        );

        RecordId recordId = RecordId.of("1234567890-0");
        MapRecord<String, String, String> message = MapRecord.create("report.trigger", messageData);
        message = message.withId(recordId);

        when(redisTemplate.opsForStream()).thenReturn(streamOperations);

        // When
        consumer.onMessage(message);

        // Then
        ArgumentCaptor<ReportTriggerV1> captor = ArgumentCaptor.forClass(ReportTriggerV1.class);
        verify(batchReportService).processReportTrigger(captor.capture());
        
        ReportTriggerV1 trigger = captor.getValue();
        assertThat(trigger.getV()).isEqualTo(1);
        assertThat(trigger.getType()).isEqualTo("INTERIM");
        assertThat(trigger.getUserId()).isEqualTo("12345");
        assertThat(trigger.getReportId()).isEqualTo(1L);
        assertThat(trigger.getMilestone()).isEqualTo(4);
        assertThat(trigger.getStatus()).isEqualTo("COLLECTING");
        assertThat(trigger.getDrivingIds()).containsExactly("trip-001", "trip-002", "trip-003", "trip-004");
        assertThat(trigger.getEmittedAt()).isEqualTo(LocalDateTime.of(2025, 1, 9, 10, 30, 0));
        assertThat(trigger.getProducer()).isEqualTo("milestone-service");
        assertThat(trigger.getTraceId()).isEqualTo("trace-123");

        // ACK 확인
        verify(streamOperations).acknowledge(eq("report.trigger"), eq("batch-report-group"), eq(recordId));
    }

    @Test
    @DisplayName("FINAL 트리거 메시지 처리")
    void testOnMessage_FinalTrigger() {
        // Given
        ReflectionTestUtils.setField(consumer, "streamKey", "report.trigger");
        ReflectionTestUtils.setField(consumer, "groupName", "batch-report-group");
        
        Map<String, String> messageData = Map.of(
                "v", "1",
                "type", "FINAL",
                "userId", "12345",
                "reportId", "1",
                "milestone", "15",
                "status", "PROCESSING",
                "drivingIds", "trip-001,trip-002,trip-015",
                "emittedAt", "2025-01-09T11:00:00",
                "producer", "milestone-service",
                "traceId", "trace-456"
        );

        RecordId recordId = RecordId.of("1234567890-1");
        MapRecord<String, String, String> message = MapRecord.create("report.trigger", messageData);
        message = message.withId(recordId);

        when(redisTemplate.opsForStream()).thenReturn(streamOperations);

        // When
        consumer.onMessage(message);

        // Then
        ArgumentCaptor<ReportTriggerV1> captor = ArgumentCaptor.forClass(ReportTriggerV1.class);
        verify(batchReportService).processReportTrigger(captor.capture());
        
        ReportTriggerV1 trigger = captor.getValue();
        assertThat(trigger.getType()).isEqualTo("FINAL");
        assertThat(trigger.getMilestone()).isEqualTo(15);
        assertThat(trigger.getStatus()).isEqualTo("PROCESSING");
        assertThat(trigger.getDrivingIds()).containsExactly("trip-001", "trip-002", "trip-015");

        // ACK 확인
        verify(streamOperations).acknowledge(eq("report.trigger"), eq("batch-report-group"), eq(recordId));
    }

    @Test
    @DisplayName("필수 필드 누락 시 기본값 처리")
    void testOnMessage_MissingFields() {
        // Given
        ReflectionTestUtils.setField(consumer, "streamKey", "report.trigger");
        ReflectionTestUtils.setField(consumer, "groupName", "batch-report-group");
        
        Map<String, String> messageData = Map.of(
                "v", "1",
                "type", "INTERIM",
                "userId", "12345",
                "reportId", "1"
                // milestone, drivingIds 등 누락
        );

        RecordId recordId = RecordId.of("1234567890-2");
        MapRecord<String, String, String> message = MapRecord.create("report.trigger", messageData);
        message = message.withId(recordId);

        when(redisTemplate.opsForStream()).thenReturn(streamOperations);

        // When
        consumer.onMessage(message);

        // Then
        ArgumentCaptor<ReportTriggerV1> captor = ArgumentCaptor.forClass(ReportTriggerV1.class);
        verify(batchReportService).processReportTrigger(captor.capture());
        
        ReportTriggerV1 trigger = captor.getValue();
        assertThat(trigger.getV()).isEqualTo(1);
        assertThat(trigger.getMilestone()).isNull();
        assertThat(trigger.getDrivingIds()).isEmpty();
        assertThat(trigger.getEmittedAt()).isNull();

        // ACK 확인
        verify(streamOperations).acknowledge(eq("report.trigger"), eq("batch-report-group"), eq(recordId));
    }

    @Test
    @DisplayName("잘못된 데이터 형식 처리")
    void testOnMessage_InvalidDataFormat() {
        // Given
        ReflectionTestUtils.setField(consumer, "streamKey", "report.trigger");
        ReflectionTestUtils.setField(consumer, "groupName", "batch-report-group");
        
        Map<String, String> messageData = Map.of(
                "type", "INTERIM",
                "userId", "12345",
                "reportId", "invalid-number", // 잘못된 숫자 형식
                "milestone", "not-a-number",  // 잘못된 숫자 형식
                "emittedAt", "invalid-date"   // 잘못된 날짜 형식
        );

        RecordId recordId = RecordId.of("1234567890-3");
        MapRecord<String, String, String> message = MapRecord.create("report.trigger", messageData);
        message = message.withId(recordId);

        when(redisTemplate.opsForStream()).thenReturn(streamOperations);

        // When
        consumer.onMessage(message);

        // Then
        ArgumentCaptor<ReportTriggerV1> captor = ArgumentCaptor.forClass(ReportTriggerV1.class);
        verify(batchReportService).processReportTrigger(captor.capture());
        
        ReportTriggerV1 trigger = captor.getValue();
        assertThat(trigger.getReportId()).isNull(); // 파싱 실패 시 null
        assertThat(trigger.getMilestone()).isNull(); // 파싱 실패 시 null
        assertThat(trigger.getEmittedAt()).isNull(); // 파싱 실패 시 null

        // ACK는 여전히 수행됨
        verify(streamOperations).acknowledge(eq("report.trigger"), eq("batch-report-group"), eq(recordId));
    }

    @Test
    @DisplayName("배치 처리 중 예외 발생 시 ACK 하지 않음")
    void testOnMessage_BatchProcessingException() {
        // Given
        ReflectionTestUtils.setField(consumer, "streamKey", "report.trigger");
        ReflectionTestUtils.setField(consumer, "groupName", "batch-report-group");
        
        Map<String, String> messageData = Map.of(
                "type", "INTERIM",
                "userId", "12345",
                "reportId", "1",
                "milestone", "4"
        );

        RecordId recordId = RecordId.of("1234567890-4");
        MapRecord<String, String, String> message = MapRecord.create("report.trigger", messageData);
        message = message.withId(recordId);

        // 예외 발생 시에는 redisTemplate.opsForStream()이 호출되지 않으므로 스텁 제거
        doThrow(new RuntimeException("Batch processing failed"))
                .when(batchReportService).processReportTrigger(any());

        // When
        consumer.onMessage(message);

        // Then
        verify(batchReportService).processReportTrigger(any());
        
        // 예외 발생 시 ACK 하지 않음 (재시도 가능하도록)
        verify(redisTemplate, never()).opsForStream();
    }

    @Test
    @DisplayName("ACK 실패 시에도 예외 발생하지 않음")
    void testOnMessage_AckFailure() {
        // Given
        ReflectionTestUtils.setField(consumer, "streamKey", "report.trigger");
        ReflectionTestUtils.setField(consumer, "groupName", "batch-report-group");
        
        Map<String, String> messageData = Map.of(
                "type", "INTERIM",
                "userId", "12345",
                "reportId", "1"
        );

        RecordId recordId = RecordId.of("1234567890-5");
        MapRecord<String, String, String> message = MapRecord.create("report.trigger", messageData);
        message = message.withId(recordId);

        when(redisTemplate.opsForStream()).thenReturn(streamOperations);
        doThrow(new RuntimeException("ACK failed"))
                .when(streamOperations).acknowledge(anyString(), anyString(), any(RecordId.class));

        // When & Then - 예외가 발생하지 않아야 함
        consumer.onMessage(message);

        verify(batchReportService).processReportTrigger(any());
        verify(streamOperations).acknowledge(eq("report.trigger"), eq("batch-report-group"), eq(recordId));
    }
}