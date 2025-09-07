package com.smooth.driving_analysis_service.reports.dna.service.impl;

import com.smooth.driving_analysis_service.reports.dna.dto.response.DnaAnalysisResponseDto;
import com.smooth.driving_analysis_service.reports.dna.entity.DnaSnapshot;
import com.smooth.driving_analysis_service.reports.dna.repository.DnaSnapshotRepository;
import com.smooth.driving_analysis_service.reports.dna.service.DnaComputeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DnaService 단위 테스트")
class DnaServiceImplTest {

    @Mock
    private DnaSnapshotRepository dnaSnapshotRepository;
    
    @Mock
    private DnaComputeService dnaComputeService;
    
    @InjectMocks
    private DnaServiceImpl dnaService;

    private DnaSnapshot mockSnapshot;

    @BeforeEach
    void setUp() {
        mockSnapshot = DnaSnapshot.builder()
                .id(1L)
                .reportId(3L)
                .userId(12345L)
                .status(DnaSnapshot.Status.FINAL)
                .code("A2-B1-C3-D2")
                .scoreA(65)
                .scoreB(35)
                .scoreC(90)
                .scoreD(65)
                .headline("안정적이면서 필요한 순간엔 과감해요!")
                .build();
    }

    @Test
    @DisplayName("정상적인 DNA 분석 결과 조회")
    void getDnaAnalysis_Success() {
        // given
        String reportId = "u1_r3_20250901";
        Long reportIdLong = 3L;
        
        when(dnaSnapshotRepository.findByReportIdAndStatus(reportIdLong, DnaSnapshot.Status.FINAL))
                .thenReturn(Optional.of(mockSnapshot));
        
        when(dnaComputeService.axisMeta("A2", "B1", "C3", "D2"))
                .thenReturn(Map.of(
                        "A", new String[]{"A2", "일반형 출발: 무리하지 않는 적절한 가속이에요."},
                        "B", new String[]{"B1", "사전 감속형: 미리 속도를 줄여 부드럽게 감속해요."},
                        "C", new String[]{"C3", "공격형: 차선 변경이 잦고 추월 성향이 있어요."},
                        "D", new String[]{"D2", "상황 유지형: 큰 무리 없이 안정적으로 대응해요."}
                ));

        // when
        DnaAnalysisResponseDto result = dnaService.getDnaAnalysis(reportId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getReportId()).isEqualTo(reportId);
        assertThat(result.getHeadline()).isEqualTo("안정적이면서 필요한 순간엔 과감해요!");
        
        // 레이더 점수 검증
        assertThat(result.getRadar().getA()).isEqualTo(65);
        assertThat(result.getRadar().getB()).isEqualTo(35);
        assertThat(result.getRadar().getC()).isEqualTo(90);
        assertThat(result.getRadar().getD()).isEqualTo(65);
        
        // 축별 정보 검증
        assertThat(result.getAxes()).hasSize(4);
        
        DnaAnalysisResponseDto.AxisDto aAxis = result.getAxes().get(0);
        assertThat(aAxis.getId()).isEqualTo("A");
        assertThat(aAxis.getLabel()).isEqualTo("A2");
        assertThat(aAxis.getSummary()).contains("일반형 출발");
        
        DnaAnalysisResponseDto.AxisDto cAxis = result.getAxes().get(2);
        assertThat(cAxis.getId()).isEqualTo("C");
        assertThat(cAxis.getLabel()).isEqualTo("C3");
        assertThat(cAxis.getSummary()).contains("공격형");
    }

    @Test
    @DisplayName("FINAL 스냅샷이 없으면 INTERIM 스냅샷 조회")
    void getDnaAnalysis_FallbackToInterim() {
        // given
        String reportId = "u1_r3_20250901";
        Long reportIdLong = 3L;
        
        DnaSnapshot interimSnapshot = DnaSnapshot.builder()
                .reportId(3L)
                .status(DnaSnapshot.Status.INTERIM)
                .code("A2-B2-C2-D2")
                .scoreA(65).scoreB(65).scoreC(65).scoreD(65)
                .headline("무리하지 않는 차분한 주행 스타일이에요!")
                .build();
        
        when(dnaSnapshotRepository.findByReportIdAndStatus(reportIdLong, DnaSnapshot.Status.FINAL))
                .thenReturn(Optional.empty());
        when(dnaSnapshotRepository.findByReportIdAndStatus(reportIdLong, DnaSnapshot.Status.INTERIM))
                .thenReturn(Optional.of(interimSnapshot));
        
        when(dnaComputeService.axisMeta("A2", "B2", "C2", "D2"))
                .thenReturn(Map.of(
                        "A", new String[]{"A2", "일반형 출발"},
                        "B", new String[]{"B2", "반응 감속형"},
                        "C", new String[]{"C2", "중립형"},
                        "D", new String[]{"D2", "상황 유지형"}
                ));

        // when
        DnaAnalysisResponseDto result = dnaService.getDnaAnalysis(reportId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getHeadline()).isEqualTo("무리하지 않는 차분한 주행 스타일이에요!");
        assertThat(result.getRadar().getA()).isEqualTo(65);
        assertThat(result.getRadar().getB()).isEqualTo(65);
        assertThat(result.getRadar().getC()).isEqualTo(65);
        assertThat(result.getRadar().getD()).isEqualTo(65);
    }

    @Test
    @DisplayName("스냅샷이 없으면 기본 응답 반환")
    void getDnaAnalysis_NoSnapshot_ReturnsDefault() {
        // given
        String reportId = "u1_r3_20250901";
        Long reportIdLong = 3L;
        
        when(dnaSnapshotRepository.findByReportIdAndStatus(eq(reportIdLong), any()))
                .thenReturn(Optional.empty());

        // when
        DnaAnalysisResponseDto result = dnaService.getDnaAnalysis(reportId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getReportId()).isEqualTo(reportId);
        assertThat(result.getHeadline()).isEqualTo("무리하지 않는 차분한 주행 스타일이에요!");
        
        // 기본 점수 (모두 65점)
        assertThat(result.getRadar().getA()).isEqualTo(65);
        assertThat(result.getRadar().getB()).isEqualTo(65);
        assertThat(result.getRadar().getC()).isEqualTo(65);
        assertThat(result.getRadar().getD()).isEqualTo(65);
        
        // 기본 축 정보
        assertThat(result.getAxes()).hasSize(4);
        assertThat(result.getAxes().get(0).getLabel()).isEqualTo("A2");
        assertThat(result.getAxes().get(1).getLabel()).isEqualTo("B2");
        assertThat(result.getAxes().get(2).getLabel()).isEqualTo("C2");
        assertThat(result.getAxes().get(3).getLabel()).isEqualTo("D2");
    }

    @Test
    @DisplayName("잘못된 DNA 코드 형식 처리")
    void getDnaAnalysis_InvalidCodeFormat_ReturnsDefault() {
        // given
        String reportId = "u1_r3_20250901";
        Long reportIdLong = 3L;
        
        DnaSnapshot invalidSnapshot = DnaSnapshot.builder()
                .reportId(3L)
                .code("INVALID-CODE") // 잘못된 형식
                .scoreA(65).scoreB(65).scoreC(65).scoreD(65)
                .headline("테스트 헤드라인")
                .build();
        
        when(dnaSnapshotRepository.findByReportIdAndStatus(reportIdLong, DnaSnapshot.Status.FINAL))
                .thenReturn(Optional.of(invalidSnapshot));

        // when
        DnaAnalysisResponseDto result = dnaService.getDnaAnalysis(reportId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getReportId()).isEqualTo(reportId);
        assertThat(result.getHeadline()).isEqualTo("무리하지 않는 차분한 주행 스타일이에요!");
    }

    @Test
    @DisplayName("reportId 파싱 테스트")
    void extractReportIdNumber_VariousFormats() {
        // 정상적인 형식들
        assertThat(dnaService.getDnaAnalysis("u1_r3_20250901").getReportId()).isEqualTo("u1_r3_20250901");
        assertThat(dnaService.getDnaAnalysis("u123_r456_20250901").getReportId()).isEqualTo("u123_r456_20250901");
        
        // 비정상적인 형식 (기본값 사용)
        assertThat(dnaService.getDnaAnalysis("invalid_format").getReportId()).isEqualTo("invalid_format");
        assertThat(dnaService.getDnaAnalysis("").getReportId()).isEqualTo("");
    }

    @Test
    @DisplayName("예외 발생 시 기본 응답 반환")
    void getDnaAnalysis_ExceptionHandling() {
        // given
        String reportId = "u1_r3_20250901";
        
        when(dnaSnapshotRepository.findByReportIdAndStatus(any(), any()))
                .thenThrow(new RuntimeException("Database error"));

        // when
        DnaAnalysisResponseDto result = dnaService.getDnaAnalysis(reportId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getReportId()).isEqualTo(reportId);
        assertThat(result.getHeadline()).isEqualTo("무리하지 않는 차분한 주행 스타일이에요!");
    }
}