package com.smooth.driving_analysis_service.batch.dto;

import com.smooth.driving_analysis_service.reports.batch.dto.ReportTriggerV1;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReportTriggerV1 DTO 테스트")
class ReportTriggerV1Test {

    @Test
    @DisplayName("INTERIM 타입 확인")
    void testIsInterim() {
        // Given
        ReportTriggerV1 trigger = ReportTriggerV1.builder()
                .type("INTERIM")
                .build();

        // When & Then
        assertThat(trigger.isInterim()).isTrue();
        assertThat(trigger.isFinal()).isFalse();
    }

    @Test
    @DisplayName("FINAL 타입 확인")
    void testIsFinal() {
        // Given
        ReportTriggerV1 trigger = ReportTriggerV1.builder()
                .type("FINAL")
                .build();

        // When & Then
        assertThat(trigger.isFinal()).isTrue();
        assertThat(trigger.isInterim()).isFalse();
    }

    @Test
    @DisplayName("알 수 없는 타입 확인")
    void testUnknownType() {
        // Given
        ReportTriggerV1 trigger = ReportTriggerV1.builder()
                .type("UNKNOWN")
                .build();

        // When & Then
        assertThat(trigger.isInterim()).isFalse();
        assertThat(trigger.isFinal()).isFalse();
    }

    @Test
    @DisplayName("null 타입 확인")
    void testNullType() {
        // Given
        ReportTriggerV1 trigger = ReportTriggerV1.builder()
                .type(null)
                .build();

        // When & Then
        assertThat(trigger.isInterim()).isFalse();
        assertThat(trigger.isFinal()).isFalse();
    }

    @Test
    @DisplayName("완전한 INTERIM 트리거 생성")
    void testCompleteInterimTrigger() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // When
        ReportTriggerV1 trigger = ReportTriggerV1.builder()
                .v(1)
                .type("INTERIM")
                .userId("12345")
                .reportId(1L)
                .milestone(4)
                .status("COLLECTING")
                .drivingIds(Arrays.asList("trip-001", "trip-002", "trip-003", "trip-004"))
                .emittedAt(now)
                .producer("milestone-service")
                .traceId("trace-123")
                .build();

        // Then
        assertThat(trigger.getV()).isEqualTo(1);
        assertThat(trigger.getType()).isEqualTo("INTERIM");
        assertThat(trigger.getUserId()).isEqualTo("12345");
        assertThat(trigger.getReportId()).isEqualTo(1L);
        assertThat(trigger.getMilestone()).isEqualTo(4);
        assertThat(trigger.getStatus()).isEqualTo("COLLECTING");
        assertThat(trigger.getDrivingIds()).hasSize(4);
        assertThat(trigger.getDrivingIds()).containsExactly("trip-001", "trip-002", "trip-003", "trip-004");
        assertThat(trigger.getEmittedAt()).isEqualTo(now);
        assertThat(trigger.getProducer()).isEqualTo("milestone-service");
        assertThat(trigger.getTraceId()).isEqualTo("trace-123");
        assertThat(trigger.isInterim()).isTrue();
        assertThat(trigger.isFinal()).isFalse();
    }

    @Test
    @DisplayName("완전한 FINAL 트리거 생성")
    void testCompleteFinalTrigger() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // When
        ReportTriggerV1 trigger = ReportTriggerV1.builder()
                .v(1)
                .type("FINAL")
                .userId("12345")
                .reportId(1L)
                .milestone(15)
                .status("PROCESSING")
                .drivingIds(Arrays.asList("trip-001", "trip-002", "trip-015"))
                .emittedAt(now)
                .producer("milestone-service")
                .traceId("trace-456")
                .build();

        // Then
        assertThat(trigger.getV()).isEqualTo(1);
        assertThat(trigger.getType()).isEqualTo("FINAL");
        assertThat(trigger.getUserId()).isEqualTo("12345");
        assertThat(trigger.getReportId()).isEqualTo(1L);
        assertThat(trigger.getMilestone()).isEqualTo(15);
        assertThat(trigger.getStatus()).isEqualTo("PROCESSING");
        assertThat(trigger.getDrivingIds()).hasSize(3);
        assertThat(trigger.getEmittedAt()).isEqualTo(now);
        assertThat(trigger.getProducer()).isEqualTo("milestone-service");
        assertThat(trigger.getTraceId()).isEqualTo("trace-456");
        assertThat(trigger.isInterim()).isFalse();
        assertThat(trigger.isFinal()).isTrue();
    }

    @Test
    @DisplayName("빈 drivingIds 처리")
    void testEmptyDrivingIds() {
        // Given & When
        ReportTriggerV1 trigger = ReportTriggerV1.builder()
                .type("INTERIM")
                .drivingIds(Collections.emptyList())
                .build();

        // Then
        assertThat(trigger.getDrivingIds()).isNotNull();
        assertThat(trigger.getDrivingIds()).isEmpty();
    }

    @Test
    @DisplayName("null drivingIds 처리")
    void testNullDrivingIds() {
        // Given & When
        ReportTriggerV1 trigger = ReportTriggerV1.builder()
                .type("INTERIM")
                .drivingIds(null)
                .build();

        // Then
        assertThat(trigger.getDrivingIds()).isNull();
    }

    @Test
    @DisplayName("NoArgsConstructor 테스트")
    void testNoArgsConstructor() {
        // Given & When
        ReportTriggerV1 trigger = new ReportTriggerV1();

        // Then
        assertThat(trigger.getType()).isNull();
        assertThat(trigger.getUserId()).isNull();
        assertThat(trigger.getReportId()).isNull();
        assertThat(trigger.getMilestone()).isNull();
        assertThat(trigger.getStatus()).isNull();
        assertThat(trigger.getDrivingIds()).isNull();
        assertThat(trigger.getEmittedAt()).isNull();
        assertThat(trigger.getProducer()).isNull();
        assertThat(trigger.getTraceId()).isNull();
        assertThat(trigger.isInterim()).isFalse();
        assertThat(trigger.isFinal()).isFalse();
    }

    @Test
    @DisplayName("AllArgsConstructor 테스트")
    void testAllArgsConstructor() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // When
        ReportTriggerV1 trigger = new ReportTriggerV1(
                1,
                "INTERIM",
                "12345",
                1L,
                4,
                "COLLECTING",
                Arrays.asList("trip-001", "trip-002"),
                now,
                "milestone-service",
                "trace-123"
        );

        // Then
        assertThat(trigger.getV()).isEqualTo(1);
        assertThat(trigger.getType()).isEqualTo("INTERIM");
        assertThat(trigger.getUserId()).isEqualTo("12345");
        assertThat(trigger.getReportId()).isEqualTo(1L);
        assertThat(trigger.getMilestone()).isEqualTo(4);
        assertThat(trigger.getStatus()).isEqualTo("COLLECTING");
        assertThat(trigger.getDrivingIds()).containsExactly("trip-001", "trip-002");
        assertThat(trigger.getEmittedAt()).isEqualTo(now);
        assertThat(trigger.getProducer()).isEqualTo("milestone-service");
        assertThat(trigger.getTraceId()).isEqualTo("trace-123");
    }

    @Test
    @DisplayName("Setter 테스트")
    void testSetters() {
        // Given
        ReportTriggerV1 trigger = new ReportTriggerV1();
        LocalDateTime now = LocalDateTime.now();

        // When
        trigger.setV(1);
        trigger.setType("FINAL");
        trigger.setUserId("67890");
        trigger.setReportId(2L);
        trigger.setMilestone(15);
        trigger.setStatus("PROCESSING");
        trigger.setDrivingIds(Arrays.asList("trip-010", "trip-015"));
        trigger.setEmittedAt(now);
        trigger.setProducer("test-producer");
        trigger.setTraceId("trace-456");

        // Then
        assertThat(trigger.getV()).isEqualTo(1);
        assertThat(trigger.getType()).isEqualTo("FINAL");
        assertThat(trigger.getUserId()).isEqualTo("67890");
        assertThat(trigger.getReportId()).isEqualTo(2L);
        assertThat(trigger.getMilestone()).isEqualTo(15);
        assertThat(trigger.getStatus()).isEqualTo("PROCESSING");
        assertThat(trigger.getDrivingIds()).containsExactly("trip-010", "trip-015");
        assertThat(trigger.getEmittedAt()).isEqualTo(now);
        assertThat(trigger.getProducer()).isEqualTo("test-producer");
        assertThat(trigger.getTraceId()).isEqualTo("trace-456");
        assertThat(trigger.isFinal()).isTrue();
        assertThat(trigger.isInterim()).isFalse();
    }
}