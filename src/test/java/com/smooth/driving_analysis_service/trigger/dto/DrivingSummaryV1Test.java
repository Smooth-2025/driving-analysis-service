package com.smooth.driving_analysis_service.trigger.dto;

import com.smooth.driving_analysis_service.reports.trigger.dto.DrivingSummaryV1;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DrivingSummaryV1 DTO 테스트")
class DrivingSummaryV1Test {

    @Test
    @DisplayName("유효성 검증 성공")
    void testValidationForProcessing_Success() {
        // Given
        DrivingSummaryV1 dto = createValidDrivingSummary();

        // When & Then
        assertDoesNotThrow(() -> dto.validateForProcessing());
    }

    @Test
    @DisplayName("필수 필드 누락 시 예외 발생")
    void testValidationForProcessing_MissingFields() {
        // Given
        DrivingSummaryV1 dto = new DrivingSummaryV1();
        dto.setDrivingId("driving-123");
        dto.setEndedAt("2025-01-09T10:30:00");
        dto.setStatus("COMPLETED");

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> dto.validateForProcessing()
        );
        assertThat(exception.getMessage()).isEqualTo("Missing required fields");
    }

    @Test
    @DisplayName("완료되지 않은 상태일 때 예외 발생")
    void testValidationForProcessing_NotCompleted() {
        // Given
        DrivingSummaryV1 dto = createValidDrivingSummary();
        dto.setStatus("PROCESSING");

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> dto.validateForProcessing()
        );
        assertThat(exception.getMessage()).isEqualTo("Only COMPLETED trips can be processed");
    }

    @Test
    @DisplayName("완료 상태 확인")
    void testIsCompleted() {
        // Given
        DrivingSummaryV1 dto = new DrivingSummaryV1();
        
        // When & Then
        dto.setStatus("COMPLETED");
        assertThat(dto.isCompleted()).isTrue();
        
        dto.setStatus("completed");
        assertThat(dto.isCompleted()).isTrue();
        
        dto.setStatus("PROCESSING");
        assertThat(dto.isCompleted()).isFalse();
        
        dto.setStatus(null);
        assertThat(dto.isCompleted()).isFalse();
    }

    @Test
    @DisplayName("필수 필드 존재 여부 확인")
    void testHasRequiredFields() {
        // Given
        DrivingSummaryV1 dto = new DrivingSummaryV1();
        
        // When & Then - 모든 필드 없음
        assertThat(dto.hasRequiredFields()).isFalse();
        
        // userId만 설정
        dto.setUserId("123");
        assertThat(dto.hasRequiredFields()).isFalse();
        
        // drivingId 추가
        dto.setDrivingId("driving-123");
        assertThat(dto.hasRequiredFields()).isFalse();
        
        // endedAt 추가
        dto.setEndedAt("2025-01-09T10:30:00");
        assertThat(dto.hasRequiredFields()).isFalse();
        
        // status 추가 - 모든 필드 완성
        dto.setStatus("COMPLETED");
        assertThat(dto.hasRequiredFields()).isTrue();
    }

    @Test
    @DisplayName("XADD 스트림 필드 매핑 확인")
    void testXaddStreamFields() {
        // Given
        DrivingSummaryV1 dto = new DrivingSummaryV1();
        
        // When - XADD 스트림 필드 설정
        dto.setDrivingMinutes(30);
        dto.setTotalDistance(15000);
        dto.setLaneChangeCount(3);
        dto.setHardBrakeCount(1);
        dto.setRapidAccelCount(2);
        
        // Then
        assertThat(dto.getDrivingMinutes()).isEqualTo(30);
        assertThat(dto.getTotalDistance()).isEqualTo(15000);
        assertThat(dto.getLaneChangeCount()).isEqualTo(3);
        assertThat(dto.getHardBrakeCount()).isEqualTo(1);
        assertThat(dto.getRapidAccelCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("시간 문자열을 LocalDateTime으로 변환")
    void testDateTimeConversion() {
        // Given
        DrivingSummaryV1 dto = new DrivingSummaryV1();
        dto.setStartedAt("2025-01-09T10:00:00");
        dto.setEndedAt("2025-01-09T10:30:00");
        
        // When
        LocalDateTime startTime = dto.getStartedAtAsDateTime();
        LocalDateTime endTime = dto.getEndedAtAsDateTime();
        
        // Then
        assertThat(startTime).isEqualTo(LocalDateTime.of(2025, 1, 9, 10, 0, 0));
        assertThat(endTime).isEqualTo(LocalDateTime.of(2025, 1, 9, 10, 30, 0));
    }

    @Test
    @DisplayName("타임스탬프를 LocalDateTime으로 변환")
    void testTimestampConversion() {
        // Given
        DrivingSummaryV1 dto = new DrivingSummaryV1();
        dto.setStartedAt("1704787200000"); // 2024-01-09 10:00:00 UTC
        dto.setEndedAt("1704789000000");   // 2024-01-09 10:30:00 UTC
        
        // When
        LocalDateTime startTime = dto.getStartedAtAsDateTime();
        LocalDateTime endTime = dto.getEndedAtAsDateTime();
        
        // Then
        assertThat(startTime).isNotNull();
        assertThat(endTime).isNotNull();
    }

    @Test
    @DisplayName("잘못된 시간 형식 처리")
    void testInvalidDateTimeFormat() {
        // Given
        DrivingSummaryV1 dto = new DrivingSummaryV1();
        dto.setStartedAt("invalid-format");
        dto.setEndedAt("also-invalid");
        
        // When
        LocalDateTime startTime = dto.getStartedAtAsDateTime();
        LocalDateTime endTime = dto.getEndedAtAsDateTime();
        
        // Then
        assertThat(startTime).isNull();
        assertThat(endTime).isNull();
    }

    private DrivingSummaryV1 createValidDrivingSummary() {
        DrivingSummaryV1 dto = new DrivingSummaryV1();
        dto.setV(1);
        dto.setUserId("12345");
        dto.setDrivingId("trip-001");
        dto.setStartedAt("2025-01-09T10:00:00");
        dto.setEndedAt("2025-01-09T10:30:00");
        dto.setStatus("COMPLETED");
        dto.setProducer("test");
        dto.setDrivingMinutes(30);
        dto.setTotalDistance(15000);
        dto.setLaneChangeCount(3);
        dto.setHardBrakeCount(1);
        dto.setRapidAccelCount(2);
        return dto;
    }
}