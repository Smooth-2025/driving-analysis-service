package com.smooth.driving_analysis_service.reports.dna.service;

import com.smooth.driving_analysis_service.reports.dna.service.impl.DnaComputeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DnaComputeServiceTest {

    @InjectMocks
    private DnaComputeServiceImpl dnaComputeService;

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
        Double sec0to40 = 11.0; // >= 10.0 for A1

        // When
        String result = dnaComputeService.classifyA(sec0to40);

        // Then
        assertEquals("A1", result); // 점진형 출발
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
        double hardBrakePerKm = 0.35; // >= 0.30 for B3

        // When
        String result = dnaComputeService.classifyB(hardBrakePerKm);

        // Then
        assertEquals("B3", result); // 급제동형
    }

    @Test
    void testClassifyB_GentleBraking() {
        // Given
        double hardBrakePerKm = 0.05; // <= 0.10 for B1

        // When
        String result = dnaComputeService.classifyB(hardBrakePerKm);

        // Then
        assertEquals("B1", result); // 사전 감속형
    }

    @Test
    void testClassifyB_ModerateBraking() {
        // Given
        double hardBrakePerKm = 0.20; // between 0.10 and 0.30 for B2

        // When
        String result = dnaComputeService.classifyB(hardBrakePerKm);

        // Then
        assertEquals("B2", result); // 반응 감속형
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
        double laneChangePerKm = 0.8; // <= 1.0 for C1

        // When
        String result = dnaComputeService.classifyC(laneChangePerKm);

        // Then
        assertEquals("C1", result); // 보수형
    }

    @Test
    void testClassifyC_ModerateLaneChange() {
        // Given
        double laneChangePerKm = 1.5; // between 1.0 and 2.0 for C2

        // When
        String result = dnaComputeService.classifyC(laneChangePerKm);

        // Then
        assertEquals("C2", result); // 중립형
    }

    @Test
    void testClassifyD_ExcellentReaction() {
        // Given
        Long reactionMs = 1500L; // <= 2000 and hasAction for D1
        Boolean responded = true;
        Boolean decel = true;
        Boolean evasive = false;

        // When
        String result = dnaComputeService.classifyD(reactionMs, responded, decel, evasive);

        // Then
        assertEquals("D1", result); // 조기 대응형
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
        assertEquals("D3", result); // 지연 대응형 (no response)
    }

    @Test
    void testClassifyD_AverageReaction() {
        // Given
        Long reactionMs = 3000L; // between 2000 and 5000
        Boolean responded = true;
        Boolean decel = true;
        Boolean evasive = false;

        // When
        String result = dnaComputeService.classifyD(reactionMs, responded, decel, evasive);

        // Then
        assertEquals("D2", result); // 상황 유지형
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
        assertEquals("D3", result); // null responded는 !Boolean.TRUE.equals(responded)로 D3
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
        assertEquals("A2-B1-C3-D2", result); // 하이픈으로 구분
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
        assertEquals(65, result.get("A")); // A2 -> 65
        assertEquals(35, result.get("B")); // B1 -> 35
        assertEquals(90, result.get("C")); // C3 -> 90
        assertEquals(65, result.get("D")); // D2 -> 65
    }

    @Test
    void testHeadline() {
        // Given - 총점 255 (65+90+65+35)
        String A = "A2"; // 65
        String B = "B3"; // 90
        String C = "C2"; // 65
        String D = "D1"; // 35

        // When
        String result = dnaComputeService.headline(A, B, C, D);

        // Then
        assertEquals("무리하지 않는 차분한 주행 스타일이에요!", result); // < 260
    }

    @Test
    void testHeadline_FastAcceleration() {
        // Given - 총점 350 (90+90+90+80)
        String A = "A3"; // 90
        String B = "B3"; // 90
        String C = "C3"; // 90
        String D = "D3"; // 90

        // When
        String result = dnaComputeService.headline(A, B, C, D);

        // Then
        assertEquals("적극적이며 빠른 반응형 운전자예요!", result); // >= 320
    }
}