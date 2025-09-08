package com.smooth.driving_analysis_service.global.redis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RedisKeys 유틸리티 테스트")
class RedisKeysTest {

    @Test
    @DisplayName("처리된 트립 키 생성")
    void testProcessedTrip() {
        // Given
        String drivingId = "trip-001";

        // When
        String key = RedisKeys.processedTrip(drivingId);

        // Then
        assertThat(key).isEqualTo("processed:trip:trip-001");
    }

    @Test
    @DisplayName("처리된 트립 키 생성 - 특수 문자 포함")
    void testProcessedTripWithSpecialChars() {
        // Given
        String drivingId = "trip-001_test@domain.com";

        // When
        String key = RedisKeys.processedTrip(drivingId);

        // Then
        assertThat(key).isEqualTo("processed:trip:trip-001_test@domain.com");
    }

    @Test
    @DisplayName("사용자별 활성 리포트 키 생성 - String 타입")
    void testActiveReportForUserString() {
        // Given
        String userId = "12345";

        // When
        String key = RedisKeys.activeReportForUser(userId);

        // Then
        assertThat(key).isEqualTo("active-report:12345");
    }

    @Test
    @DisplayName("사용자별 활성 리포트 키 생성 - Long 타입")
    void testActiveReportForUserLong() {
        // Given
        Long userId = 12345L;

        // When
        String key = RedisKeys.activeReportForUser(userId);

        // Then
        assertThat(key).isEqualTo("active-report:12345");
    }

    @Test
    @DisplayName("스트림 키 상수 확인")
    void testStreamKeys() {
        // When & Then
        assertThat(RedisKeys.DRIVING_ANALYSIS_STREAM).isEqualTo("driving-analysis-stream");
        assertThat(RedisKeys.REPORT_TRIGGER_STREAM).isEqualTo("report.trigger");
    }

    @Test
    @DisplayName("null 값 처리")
    void testNullValues() {
        // Given
        String nullDrivingId = null;
        String nullUserId = null;
        Long nullUserIdLong = null;

        // When
        String processedTripKey = RedisKeys.processedTrip(nullDrivingId);
        String activeReportKeyString = RedisKeys.activeReportForUser(nullUserId);
        String activeReportKeyLong = RedisKeys.activeReportForUser(nullUserIdLong);

        // Then
        assertThat(processedTripKey).isEqualTo("processed:trip:null");
        assertThat(activeReportKeyString).isEqualTo("active-report:null");
        assertThat(activeReportKeyLong).isEqualTo("active-report:null");
    }

    @Test
    @DisplayName("빈 문자열 처리")
    void testEmptyStrings() {
        // Given
        String emptyDrivingId = "";
        String emptyUserId = "";

        // When
        String processedTripKey = RedisKeys.processedTrip(emptyDrivingId);
        String activeReportKey = RedisKeys.activeReportForUser(emptyUserId);

        // Then
        assertThat(processedTripKey).isEqualTo("processed:trip:");
        assertThat(activeReportKey).isEqualTo("active-report:");
    }

    @Test
    @DisplayName("공백 문자열 처리")
    void testWhitespaceStrings() {
        // Given
        String whitespaceDrivingId = "   ";
        String whitespaceUserId = "   ";

        // When
        String processedTripKey = RedisKeys.processedTrip(whitespaceDrivingId);
        String activeReportKey = RedisKeys.activeReportForUser(whitespaceUserId);

        // Then
        assertThat(processedTripKey).isEqualTo("processed:trip:   ");
        assertThat(activeReportKey).isEqualTo("active-report:   ");
    }

    @Test
    @DisplayName("매우 긴 ID 처리")
    void testLongIds() {
        // Given
        String longDrivingId = "trip-" + "a".repeat(100);
        String longUserId = "user-" + "b".repeat(100);

        // When
        String processedTripKey = RedisKeys.processedTrip(longDrivingId);
        String activeReportKey = RedisKeys.activeReportForUser(longUserId);

        // Then
        assertThat(processedTripKey).startsWith("processed:trip:trip-");
        assertThat(processedTripKey).hasSize("processed:trip:trip-".length() + 100);
        assertThat(activeReportKey).startsWith("active-report:user-");
        assertThat(activeReportKey).hasSize("active-report:user-".length() + 100);
    }

    @Test
    @DisplayName("숫자 ID 처리")
    void testNumericIds() {
        // Given
        String numericDrivingId = "123456789";
        Long numericUserId = 987654321L;

        // When
        String processedTripKey = RedisKeys.processedTrip(numericDrivingId);
        String activeReportKey = RedisKeys.activeReportForUser(numericUserId);

        // Then
        assertThat(processedTripKey).isEqualTo("processed:trip:123456789");
        assertThat(activeReportKey).isEqualTo("active-report:987654321");
    }
}