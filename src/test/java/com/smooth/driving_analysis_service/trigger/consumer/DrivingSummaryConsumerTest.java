package com.smooth.driving_analysis_service.trigger.consumer;

import com.smooth.driving_analysis_service.reports.trigger.consumer.DrivingSummaryConsumer;
import com.smooth.driving_analysis_service.reports.trigger.dto.DrivingSummaryV1;
import com.smooth.driving_analysis_service.reports.trigger.service.DrivingSummaryConsumerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DrivingSummaryConsumerTest {

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private StreamOperations<String, Object, Object> streamOperations;

    @Mock
    private DrivingSummaryConsumerService service;

    @InjectMocks
    private DrivingSummaryConsumer consumer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(consumer, "streamKey", "driving-analysis-stream");
        ReflectionTestUtils.setField(consumer, "groupName", "driving-analyzer-group");
        lenient().when(redis.opsForStream()).thenReturn(streamOperations);
    }

    @Test
    void testOnMessage_Success() {
        // Given
        Map<String, String> messageData = new HashMap<>();
        messageData.put("v", "1");
        messageData.put("userId", "123");
        messageData.put("drivingId", "driving-123");
        messageData.put("endedAt", String.valueOf(System.currentTimeMillis()));
        messageData.put("status", "COMPLETED");
        messageData.put("producer", "test-producer");
        messageData.put("drivingMinutes", "1800");
        messageData.put("totalDistance", "15500");
        messageData.put("avgSpeed", "45.2");
        messageData.put("maxSpeed", "80.0");
        messageData.put("minSpeed", "10.0");
        messageData.put("cruiseRatio", "0.75");
        messageData.put("laneChangeCount", "5");
        messageData.put("hardBrakeCount", "2");
        messageData.put("rapidAccelCount", "3");
        messageData.put("sharpTurnCount", "1");

        RecordId recordId = RecordId.of("1234567890-0");
        MapRecord<String, String, String> message = StreamRecords.mapBacked(messageData)
            .withId(recordId)
            .withStreamKey("driving-analysis-stream");

        // When
        consumer.onMessage(message);

        // Then
        ArgumentCaptor<DrivingSummaryV1> dtoCaptor = ArgumentCaptor.forClass(DrivingSummaryV1.class);
        verify(service).processDrivingSummary(eq("1234567890-0"), dtoCaptor.capture());
        
        DrivingSummaryV1 capturedDto = dtoCaptor.getValue();
        assertEquals(1, capturedDto.getV());
        assertEquals("123", capturedDto.getUserId());
        assertEquals("driving-123", capturedDto.getDrivingId());
        assertEquals("COMPLETED", capturedDto.getStatus());
        assertEquals("test-producer", capturedDto.getProducer());
        assertEquals(1800, capturedDto.getDrivingMinutes());
        assertEquals(15500, capturedDto.getTotalDistance());
        assertEquals(45.2, capturedDto.getAvgSpeed());
        // maxSpeed and minSpeed methods not available in DrivingSummaryV1
        assertEquals(0.75, capturedDto.getCruiseRatio());
        assertEquals(5, capturedDto.getLaneChangeCount());
        assertEquals(2, capturedDto.getHardBrakeCount());
        assertEquals(3, capturedDto.getRapidAccelCount());
        // sharpTurnCount method not available in DrivingSummaryV1

        // ACK 확인
        verify(streamOperations).acknowledge("driving-analysis-stream", "driving-analyzer-group", recordId);
    }

    @Test
    void testOnMessage_WithNullValues() {
        // Given
        Map<String, String> messageData = new HashMap<>();
        messageData.put("userId", "123");
        messageData.put("drivingId", "driving-123");
        messageData.put("endedAt", String.valueOf(System.currentTimeMillis()));
        messageData.put("status", "COMPLETED");
        messageData.put("producer", "test-producer");
        // 다른 필드들은 null/없음

        RecordId recordId = RecordId.of("1234567890-0");
        MapRecord<String, String, String> message = StreamRecords.mapBacked(messageData)
            .withId(recordId)
            .withStreamKey("driving-analysis-stream");

        // When
        consumer.onMessage(message);

        // Then
        ArgumentCaptor<DrivingSummaryV1> dtoCaptor = ArgumentCaptor.forClass(DrivingSummaryV1.class);
        verify(service).processDrivingSummary(eq("1234567890-0"), dtoCaptor.capture());
        
        DrivingSummaryV1 capturedDto = dtoCaptor.getValue();
        assertEquals(1, capturedDto.getV()); // default value when null
        assertEquals("123", capturedDto.getUserId());
        assertEquals("driving-123", capturedDto.getDrivingId());
        assertNull(capturedDto.getDrivingMinutes());
        assertNull(capturedDto.getTotalDistance());
        assertNull(capturedDto.getAvgSpeed());
        assertNull(capturedDto.getLaneChangeCount());
        assertNull(capturedDto.getHardBrakeCount());
    }

    @Test
    void testOnMessage_WithInvalidNumbers() {
        // Given
        Map<String, String> messageData = new HashMap<>();
        messageData.put("v", "invalid");
        messageData.put("userId", "123");
        messageData.put("drivingId", "driving-123");
        messageData.put("endedAt", "invalid-timestamp");
        messageData.put("status", "COMPLETED");
        messageData.put("producer", "test-producer");
        messageData.put("drivingMinutes", "invalid-int");
        messageData.put("totalDistance", "invalid-double");

        RecordId recordId = RecordId.of("1234567890-0");
        MapRecord<String, String, String> message = StreamRecords.mapBacked(messageData)
            .withId(recordId)
            .withStreamKey("driving-analysis-stream");

        // When
        consumer.onMessage(message);

        // Then
        ArgumentCaptor<DrivingSummaryV1> dtoCaptor = ArgumentCaptor.forClass(DrivingSummaryV1.class);
        verify(service).processDrivingSummary(eq("1234567890-0"), dtoCaptor.capture());
        
        DrivingSummaryV1 capturedDto = dtoCaptor.getValue();
        assertEquals(1, capturedDto.getV()); // default when parsing fails
        assertEquals("123", capturedDto.getUserId());
        assertEquals("invalid-timestamp", capturedDto.getEndedAt());
        assertNull(capturedDto.getDrivingMinutes());
        assertNull(capturedDto.getTotalDistance());
    }

    @Test
    void testOnMessage_ServiceThrowsException() {
        // Given
        Map<String, String> messageData = new HashMap<>();
        messageData.put("userId", "123");
        messageData.put("drivingId", "driving-123");
        messageData.put("status", "COMPLETED");

        RecordId recordId = RecordId.of("1234567890-0");
        MapRecord<String, String, String> message = StreamRecords.mapBacked(messageData)
            .withId(recordId)
            .withStreamKey("driving-analysis-stream");

        doThrow(new RuntimeException("Service error")).when(service)
            .processDrivingSummary(any(), any());

        // When & Then - 예외가 발생해도 메서드가 정상 종료되어야 함
        assertDoesNotThrow(() -> consumer.onMessage(message));
        
        // ACK는 호출되지 않아야 함 (예외 발생으로 인해)
        verify(streamOperations, never()).acknowledge(any(String.class), any(String.class), any(RecordId.class));
    }
}