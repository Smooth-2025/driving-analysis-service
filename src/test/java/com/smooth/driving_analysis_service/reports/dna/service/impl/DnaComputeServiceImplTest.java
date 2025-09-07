package com.smooth.driving_analysis_service.reports.dna.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("DnaComputeService 단위 테스트")
class DnaComputeServiceImplTest {

    @InjectMocks
    private DnaComputeServiceImpl dnaComputeService;

    @Test
    @DisplayName("A축 분류 - 출발 성향")
    void classifyA_AccelerationDna() {
        // A1: 점진형 (≥10초)
        assertThat(dnaComputeService.classifyA(12.0)).isEqualTo("A1");
        assertThat(dnaComputeService.classifyA(10.0)).isEqualTo("A1");
        
        // A3: 빠른 스타트형 (≤5초)
        assertThat(dnaComputeService.classifyA(3.0)).isEqualTo("A3");
        assertThat(dnaComputeService.classifyA(5.0)).isEqualTo("A3");
        
        // A2: 일반형 (5~10초)
        assertThat(dnaComputeService.classifyA(7.0)).isEqualTo("A2");
        assertThat(dnaComputeService.classifyA(8.5)).isEqualTo("A2");
        
        // null 처리
        assertThat(dnaComputeService.classifyA(null)).isEqualTo("A2");
    }

    @Test
    @DisplayName("B축 분류 - 감속 성향")
    void classifyB_DecelerationDna() {
        // B3: 급제동형 (≥0.30 회/km)
        assertThat(dnaComputeService.classifyB(0.35)).isEqualTo("B3");
        assertThat(dnaComputeService.classifyB(0.30)).isEqualTo("B3");
        
        // B1: 사전 감속형 (≤0.10 회/km)
        assertThat(dnaComputeService.classifyB(0.05)).isEqualTo("B1");
        assertThat(dnaComputeService.classifyB(0.10)).isEqualTo("B1");
        
        // B2: 반응 감속형 (0.10~0.30 회/km)
        assertThat(dnaComputeService.classifyB(0.15)).isEqualTo("B2");
        assertThat(dnaComputeService.classifyB(0.25)).isEqualTo("B2");
    }

    @Test
    @DisplayName("C축 분류 - 차선 변경 성향")
    void classifyC_LaneBehaviorDna() {
        // C1: 보수형 (≤1.0 회/km)
        assertThat(dnaComputeService.classifyC(0.5)).isEqualTo("C1");
        assertThat(dnaComputeService.classifyC(1.0)).isEqualTo("C1");
        
        // C3: 공격형 (≥2.0 회/km)
        assertThat(dnaComputeService.classifyC(2.5)).isEqualTo("C3");
        assertThat(dnaComputeService.classifyC(2.0)).isEqualTo("C3");
        
        // C2: 중립형 (1.0~2.0 회/km)
        assertThat(dnaComputeService.classifyC(1.5)).isEqualTo("C2");
        assertThat(dnaComputeService.classifyC(1.8)).isEqualTo("C2");
    }

    @Test
    @DisplayName("D축 분류 - 사고 대응 성향")
    void classifyD_SituationalResponseDna() {
        // D1: 조기 대응형 (≤2초 + 반응 + 조작)
        assertThat(dnaComputeService.classifyD(1500L, true, true, false)).isEqualTo("D1");
        assertThat(dnaComputeService.classifyD(2000L, true, false, true)).isEqualTo("D1");
        
        // D3: 지연 대응형 (≥5초 또는 무반응)
        assertThat(dnaComputeService.classifyD(6000L, true, true, false)).isEqualTo("D3");
        assertThat(dnaComputeService.classifyD(3000L, false, false, false)).isEqualTo("D3");
        assertThat(dnaComputeService.classifyD(null, false, false, false)).isEqualTo("D3");
        
        // D2: 상황 유지형 (2~5초)
        assertThat(dnaComputeService.classifyD(3000L, true, false, false)).isEqualTo("D2");
        assertThat(dnaComputeService.classifyD(4000L, true, true, false)).isEqualTo("D2");
    }

    @Test
    @DisplayName("DNA 코드 생성")
    void toCode_DnaCodeGeneration() {
        String code = dnaComputeService.toCode("A2", "B1", "C3", "D2");
        assertThat(code).isEqualTo("A2-B1-C3-D2");
    }

    @Test
    @DisplayName("레이더 점수 생성")
    void toRadar_RadarScoreGeneration() {
        Map<String, Integer> radar = dnaComputeService.toRadar("A1", "B2", "C3", "D2");
        
        assertThat(radar.get("A")).isEqualTo(35);  // A1 -> 35점
        assertThat(radar.get("B")).isEqualTo(65);  // B2 -> 65점
        assertThat(radar.get("C")).isEqualTo(90);  // C3 -> 90점
        assertThat(radar.get("D")).isEqualTo(65);  // D2 -> 65점
    }

    @Test
    @DisplayName("축별 메타데이터 생성")
    void axisMeta_AxisMetadataGeneration() {
        Map<String, String[]> axisMeta = dnaComputeService.axisMeta("A2", "B1", "C3", "D2");
        
        // A축 메타데이터
        String[] aMeta = axisMeta.get("A");
        assertThat(aMeta[0]).isEqualTo("A2");
        assertThat(aMeta[1]).contains("일반형 출발");
        
        // B축 메타데이터
        String[] bMeta = axisMeta.get("B");
        assertThat(bMeta[0]).isEqualTo("B1");
        assertThat(bMeta[1]).contains("사전 감속형");
        
        // C축 메타데이터
        String[] cMeta = axisMeta.get("C");
        assertThat(cMeta[0]).isEqualTo("C3");
        assertThat(cMeta[1]).contains("공격형");
        
        // D축 메타데이터
        String[] dMeta = axisMeta.get("D");
        assertThat(dMeta[0]).isEqualTo("D2");
        assertThat(dMeta[1]).contains("상황 유지형");
    }

    @Test
    @DisplayName("헤드라인 생성")
    void headline_HeadlineGeneration() {
        // 고득점 (320점 이상) - A3(90) + B3(90) + C3(90) + D3(90) = 360점
        String highScore = dnaComputeService.headline("A3", "B3", "C3", "D3");
        assertThat(highScore).contains("적극적이며 빠른 반응형");
        
        // 중간점수 (260~319점) - A2(65) + B2(65) + C3(90) + D2(65) = 285점
        String midScore = dnaComputeService.headline("A2", "B2", "C3", "D2");
        assertThat(midScore).contains("안정적이면서 필요한 순간엔 과감");
        
        // 저득점 (260점 미만) - A1(35) + B1(35) + C1(35) + D1(35) = 140점
        String lowScore = dnaComputeService.headline("A1", "B1", "C1", "D1");
        assertThat(lowScore).contains("무리하지 않는 차분한 주행");
    }
}