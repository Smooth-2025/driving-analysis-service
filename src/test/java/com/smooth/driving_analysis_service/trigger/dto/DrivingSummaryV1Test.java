package com.smooth.driving_analysis_service.trigger.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DrivingSummaryV1Test {

    @Test
    void testValidationForProcessing_Success() {
        // Given
        DrivingSummaryV1 dto = new DrivingSummaryV1();
        dto.setUserId("123");
        dto.setDrivingId("driving-123");
        dto.setEndedAt(System.currentTimeMillis());
        dto.setStatus("COMPLETED");

        // When & Then
        assertDoesNotThrow(() -> dto.validateForProcessing());
    }

    @Test
    void testValidationForProcessing_MissingUserId() {
        // Given
        DrivingSummaryV1 dto = new DrivingSummaryV1();
        dto.setDrivingId("driving-123");
        dto.setEndedAt(System.currentTimeMillis());
        dto.setStatus("COMPLETED");

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> dto.validateForProcessing()
        );
        assertEquals("Missing required fields", exception.getMessage());
    }

    @Test
    void testValidationForProcessing_NotCompleted() {
        // Given
        DrivingSummaryV1 dto = new DrivingSummaryV1();
        dto.setUserId("123");
        dto.setDrivingId("driving-123");
        dto.setEndedAt(System.currentTimeMillis());
        dto.setStatus("PROCESSING");

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> dto.validateForProcessing()
        );
        assertEquals("Only COMPLETED trips can be processed", exception.getMessage());
    }

    @Test
    void testIsCompleted() {
        // Given
        DrivingSummaryV1 dto = new DrivingSummaryV1();
        
        // When & Then
        dto.setStatus("COMPLETED");
        assertTrue(dto.isCompleted());
        
        dto.setStatus("completed");
        assertTrue(dto.isCompleted());
        
        dto.setStatus("PROCESSING");
        assertFalse(dto.isCompleted());
        
        dto.setStatus(null);
        assertFalse(dto.isCompleted());
    }

    @Test
    void testHasRequiredFields() {
        // Given
        DrivingSummaryV1 dto = new DrivingSummaryV1();
        
        // When & Then - 모든 필드 없음
        assertFalse(dto.hasRequiredFields());
        
        // userId만 설정
        dto.setUserId("123");
        assertFalse(dto.hasRequiredFields());
        
        // drivingId 추가
        dto.setDrivingId("driving-123");
        assertFalse(dto.hasRequiredFields());
        
        // endedAt 추가 (0은 유효하지 않음)
        dto.setEndedAt(0L);
        assertFalse(dto.hasRequiredFields());
        
        // endedAt 유효한 값으로 설정
        dto.setEndedAt(System.currentTimeMillis());
        assertFalse(dto.hasRequiredFields());
        
        // status 추가 - 모든 필드 완성
        dto.setStatus("COMPLETED");
        assertTrue(dto.hasRequiredFields());
    }

    @Test
    void testDrivingRecordFieldsMapping() {
        // Given
        DrivingSummaryV1 dto = new DrivingSummaryV1();
        
        // When - DrivingRecord와 매핑되는 모든 필드 설정
        dto.setTotalDistance(15.5);
        dto.setAvgSpeed(45.2);
        dto.setMaxSpeed(80.0);
        dto.setMinSpeed(10.0);
        dto.setCruiseRatio(0.75);
        dto.setLaneChangeCount(5);
        dto.setHardBrakeCount(2);
        dto.setRapidAccelCount(3);
        dto.setSharpTurnCount(1);
        
        // Then - 모든 필드가 정상적으로 설정되었는지 확인
        assertEquals(15.5, dto.getTotalDistance());
        assertEquals(45.2, dto.getAvgSpeed());
        assertEquals(80.0, dto.getMaxSpeed());
        assertEquals(10.0, dto.getMinSpeed());
        assertEquals(0.75, dto.getCruiseRatio());
        assertEquals(5, dto.getLaneChangeCount());
        assertEquals(2, dto.getHardBrakeCount());
        assertEquals(3, dto.getRapidAccelCount());
        assertEquals(1, dto.getSharpTurnCount());
    }
}