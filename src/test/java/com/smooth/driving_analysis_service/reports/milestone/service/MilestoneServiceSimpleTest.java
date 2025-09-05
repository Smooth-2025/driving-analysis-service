package com.smooth.driving_analysis_service.reports.milestone.service;

import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MilestoneService의 간단한 단위 테스트
 * 복잡한 Mock 설정 없이 핵심 로직만 테스트
 */
class MilestoneServiceSimpleTest {

    @Test
    @DisplayName("MilestoneReport 생성 테스트")
    void testMilestoneReportCreation() {
        // Given
        Long userId = 12345L;
        int cycleNo = 1;
        
        // When
        MilestoneReport report = MilestoneReport.newCollecting(userId, cycleNo);
        
        // Then
        assertThat(report.getUserId()).isEqualTo(userId);
        assertThat(report.getCycleNo()).isEqualTo(cycleNo);
        assertThat(report.getNumberOfDriving()).isEqualTo(0);
        assertThat(report.getStatus()).isEqualTo(MilestoneReport.Status.COLLECTING);
        assertThat(report.isRead()).isFalse();
    }

    @Test
    @DisplayName("MilestoneReport 상태 변경 테스트")
    void testMilestoneReportStatusChange() {
        // Given
        MilestoneReport report = MilestoneReport.newCollecting(12345L, 1);
        
        // When & Then - COLLECTING -> PROCESSING
        report.setStatus(MilestoneReport.Status.PROCESSING);
        assertThat(report.getStatus()).isEqualTo(MilestoneReport.Status.PROCESSING);
        
        // When & Then - PROCESSING -> COMPLETED
        report.setStatus(MilestoneReport.Status.COMPLETED);
        assertThat(report.getStatus()).isEqualTo(MilestoneReport.Status.COMPLETED);
    }

    @Test
    @DisplayName("MilestoneReport numberOfDriving 증가 테스트")
    void testNumberOfDrivingIncrement() {
        // Given
        MilestoneReport report = MilestoneReport.newCollecting(12345L, 1);
        
        // When & Then
        for (int i = 1; i <= 15; i++) {
            report.setNumberOfDriving(i);
            assertThat(report.getNumberOfDriving()).isEqualTo(i);
        }
    }

    @Test
    @DisplayName("MilestoneReport reportId 설정 테스트")
    void testReportIdSetting() {
        // Given
        MilestoneReport report = MilestoneReport.builder()
                .userId(12345L)
                .cycleNo(3)
                .reportId("u12345_r3_20250109")
                .build();
        
        // When & Then
        assertThat(report.getReportId()).isEqualTo("u12345_r3_20250109");
        assertThat(report.getReportId()).startsWith("u12345_r3_");
    }

    @Test
    @DisplayName("MilestoneReport 편의 메서드 테스트")
    void testConvenienceMethods() {
        // Given
        MilestoneReport report = MilestoneReport.builder()
                .cycleNo(5)
                .build();
        
        // When & Then
        assertThat(report.getReportNo()).isEqualTo(5);
        assertThat(report.getLabel()).isEqualTo("report(5)");
    }
}