package com.smooth.driving_analysis_service.reports.dna.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DnaComputeServiceTest {

    @InjectMocks
    private DnaComputeService dnaComputeService;

    @Test
    void testClassifyA_RapidAcceleration() {
        // Given
        Double sec0to40 = 3.5;

        // When
        String result = dnaComputeService.classifyA(sec0to40);

        // Then
        assertEquals("A3", result); // 급가속형
    }

    @Test
    void testClassifyA_GentleAcceleration() {
        // Given
        Double sec0to40 = 9.0;

        // When
        String result = dnaComputeService.classifyA(sec0to40);

        // Then
        assertEquals("A1", result); // 완만가속형
    }

    @Test
    void testClassifyA_ModerateAcceleration() {
        // Given
        Double sec0to40 = 6.0;

        // When
        String result = dnaComputeService.classifyA(sec0to40);

        // Then
        assertEquals("A2", result); // 중간
    }

    @Test
    void testClassifyA_NullValue() {
        // Given
        Double sec0to40 = null;

        // When
        String result = dnaComputeService.classifyA(sec0to40);

        // Then
        assertEquals("A2", result); // 기본값
    }

    @Test
    void testClassifyB_HardBraking() {
        // Given
        double hardBrakePerKm = 0.6;

        // When
        String result = dnaComputeService.classifyB(hardBrakePerKm);

        // Then
        assertEquals("B3", result); // 급감속형
    }

    @Test
    void testClassifyB_GentleBraking() {
        // Given
        double hardBrakePerKm = 0.05;

        // When
        String result = dnaComputeService.classifyB(hardBrakePerKm);

        // Then
        assertEquals("B1", result); // 사전감속형
    }

    @Test
    void testClassifyB_ModerateBraking() {
        // Given
        double hardBrakePerKm = 0.3;

        // When
        String result = dnaComputeService.classifyB(hardBrakePerKm);

        // Then
        assertEquals("B2", result); // 중간
    }

    @Test
    void testClassifyC_AggressiveLaneChange() {
        // Given
        double laneChangePerKm = 2.5;

        // When
        String result = dnaComputeService.classifyC(laneChangePerKm);

        // Then
        assertEquals("C3", result); // 적극형
    }

    @Test
    void testClassifyC_CautiousLaneChange() {
        // Given
        double laneChangePerKm = 0.3;

        // When
        String result = dnaComputeService.classifyC(laneChangePerKm);

        // Then
        assertEquals("C1", result); // 신중형
    }

    @Test
    void testClassifyC_ModerateLaneChange() {
        // Given
        double laneChangePerKm = 1.0;

        // When
        String result = dnaComputeService.classifyC(laneChangePerKm);

        // Then
        assertEquals("C2", result); // 중간
    }

    @Test
    void testClassifyD_ExcellentReaction() {
        // Given
        Long reactionMs = 800L;
        Boolean responded = true;
        Boolean decel = true;
        Boolean evasive = true;

        // When
        String result = dnaComputeService.classifyD(reactionMs, responded, decel, evasive);

        // Then
        assertEquals("D3", result); // 우수
    }

    @Test
    void testClassifyD_NoResponse() {
        // Given
        Long reactionMs = 1500L;
        Boolean responded = false;
        Boolean decel = false;
        Boolean evasive = false;

        // When
        String result = dnaComputeService.classifyD(reactionMs, responded, decel, evasive);

        // Then
        assertEquals("D1", result); // 미대응
    }

    @Test
    void testClassifyD_AverageReaction() {
        // Given
        Long reactionMs = 1200L;
        Boolean responded = true;
        Boolean decel = true;
        Boolean evasive = false;

        // When
        String result = dnaComputeService.classifyD(reactionMs, responded, decel, evasive);

        // Then
        assertEquals("D2", result); // 보통
    }

    @Test
    void testClassifyD_NullValues() {
        // Given
        Long reactionMs = null;
        Boolean responded = null;
        Boolean decel = null;
        Boolean evasive = null;

        // When
        String result = dnaComputeService.classifyD(reactionMs, responded, decel, evasive);

        // Then
        assertEquals("D2", result); // 기본값
    }

    @Test
    void testToCode() {
        // Given
        String A = "A2";
        String B = "B1";
        String C = "C3";
        String D = "D2";

        // When
        String result = dnaComputeService.toCode(A, B, C, D);

        // Then
        assertEquals("A2B1C3D2", result);
    }

    @Test
    void testToRadar() {
        // Given
        String A = "A2";
        String B = "B1";
        String C = "C3";
        String D = "D2";

        // When
        Map<String, Integer> result = dnaComputeService.toRadar(A, B, C, D);

        // Then
        assertEquals(4, result.size());
        assertEquals(2, result.get("A"));
        assertEquals(1, result.get("B"));
        assertEquals(3, result.get("C"));
        assertEquals(2, result.get("D"));
    }

    @Test
    void testHeadline() {
        // Given
        String A = "A1"; // 완만한 가속
        String B = "B3"; // 급감속
        String C = "C2";
        String D = "D1";

        // When
        String result = dnaComputeService.headline(A, B, C, D);

        // Then
        assertEquals("완만한 가속 · 급감속 성향", result);
    }

    @Test
    void testHeadline_FastAcceleration() {
        // Given
        String A = "A3"; // 빠른 가속
        String B = "B1"; // 사전 감속
        String C = "C2";
        String D = "D1";

        // When
        String result = dnaComputeService.headline(A, B, C, D);

        // Then
        assertEquals("빠른 가속 · 사전 감속 성향", result);
    }
}