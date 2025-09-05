package com.smooth.driving_analysis_service.reports.behavior.service;

import com.smooth.driving_analysis_service.reports.behavior.dto.response.BehaviorAnalysisResponseDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.CompareDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.TotalCountsDto;
import com.smooth.driving_analysis_service.reports.behavior.repository.BehaviorTotalCountsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BehaviorReportService Task 3 테스트")
class BehaviorReportServiceImplTask3Test {

    @Mock
    private BehaviorTotalCountsRepository totalCountsRepository;
    
    @Mock
    private BehaviorCompareAnalyzer compareAnalyzer;

    @InjectMocks
    private BehaviorReportServiceImpl behaviorReportService;

    @Test
    @DisplayName("Task 3: totalCounts + compare 정상 조회")
    void getBehaviorAnalysis_WithCompare_Success() {
        // given
        String reportId = "u1_r3_20250901";
        TotalCountsDto mockTotalCounts = TotalCountsDto.of(38, 42, 17);
        CompareDto mockCompare = createMockCompare();
        
        when(totalCountsRepository.findTotalCountsByReportId(3L))
                .thenReturn(mockTotalCounts);
        when(compareAnalyzer.analyzeCompare(anyLong(), anyTotalCountsDto()))
                .thenReturn(mockCompare);

        // when
        BehaviorAnalysisResponseDto result = behaviorReportService.getBehaviorAnalysis(reportId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getReportId()).isEqualTo(reportId);
        
        // Task 1: totalCounts 검증
        assertThat(result.getTotalCounts()).isNotNull();
        assertThat(result.getTotalCounts().getHardBrake()).isEqualTo(38);
        assertThat(result.getTotalCounts().getRapidAccel()).isEqualTo(42);
        assertThat(result.getTotalCounts().getLaneChange()).isEqualTo(17);
        assertThat(result.getTotalCounts().getTotal()).isEqualTo(97);
        
        // Task 3: compare 검증
        assertThat(result.getCompare()).isNotNull();
        assertThat(result.getCompare().getIncdec()).isEqualTo(7.78);
        assertThat(result.getCompare().getChart()).isNotNull();
        assertThat(result.getCompare().getChart().getHardBrake().getBefore()).isEqualTo(35);
        assertThat(result.getCompare().getChart().getHardBrake().getCurrent()).isEqualTo(38);
        
        // drivingPattern은 임시 기본값
        assertThat(result.getDrivingPattern()).isNotNull();
        assertThat(result.getDrivingPattern().getWeekday()).isEqualTo("금요일");
    }

    @Test
    @DisplayName("Task 3: 첫 번째 리포트 (이전 데이터 없음)")
    void getBehaviorAnalysis_FirstReport() {
        // given
        String reportId = "u1_r1_20250901";
        TotalCountsDto mockTotalCounts = TotalCountsDto.of(20, 25, 10);
        CompareDto mockCompare = createFirstReportCompare();
        
        when(totalCountsRepository.findTotalCountsByReportId(1L))
                .thenReturn(mockTotalCounts);
        when(compareAnalyzer.analyzeCompare(anyLong(), anyTotalCountsDto()))
                .thenReturn(mockCompare);

        // when
        BehaviorAnalysisResponseDto result = behaviorReportService.getBehaviorAnalysis(reportId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCompare().getIncdec()).isEqualTo(100.0); // 첫 리포트는 100% 증가
        assertThat(result.getCompare().getChart().getHardBrake().getBefore()).isEqualTo(0);
        assertThat(result.getCompare().getChart().getHardBrake().getCurrent()).isEqualTo(20);
    }

    @Test
    @DisplayName("Task 3: 감소 패턴 분석")
    void getBehaviorAnalysis_DecreasePattern() {
        // given
        String reportId = "u1_r4_20250901";
        TotalCountsDto mockTotalCounts = TotalCountsDto.of(30, 35, 10);
        CompareDto mockCompare = createDecreaseCompare();
        
        when(totalCountsRepository.findTotalCountsByReportId(4L))
                .thenReturn(mockTotalCounts);
        when(compareAnalyzer.analyzeCompare(anyLong(), anyTotalCountsDto()))
                .thenReturn(mockCompare);

        // when
        BehaviorAnalysisResponseDto result = behaviorReportService.getBehaviorAnalysis(reportId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCompare().getIncdec()).isEqualTo(-25.0); // 25% 감소
    }

    @Test
    @DisplayName("Task 3: 예외 발생시 기본 응답")
    void getBehaviorAnalysis_ExceptionHandling() {
        // given
        String reportId = "invalid_format";
        
        when(totalCountsRepository.findTotalCountsByReportId(anyLong()))
                .thenThrow(new RuntimeException("Database error"));

        // when
        BehaviorAnalysisResponseDto result = behaviorReportService.getBehaviorAnalysis(reportId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getReportId()).isEqualTo(reportId);
        assertThat(result.getTotalCounts().getTotal()).isEqualTo(0);
        assertThat(result.getCompare()).isNotNull();
        assertThat(result.getCompare().getIncdec()).isEqualTo(0.0);
    }

    private CompareDto createMockCompare() {
        return CompareDto.builder()
                .incdec(7.78)
                .chart(CompareDto.ChartDto.builder()
                        .hardBrake(CompareDto.BeforeAfterDto.builder().before(35).current(38).build())
                        .rapidAccel(CompareDto.BeforeAfterDto.builder().before(40).current(42).build())
                        .laneChange(CompareDto.BeforeAfterDto.builder().before(15).current(17).build())
                        .build())
                .build();
    }

    private CompareDto createFirstReportCompare() {
        return CompareDto.builder()
                .incdec(100.0)
                .chart(CompareDto.ChartDto.builder()
                        .hardBrake(CompareDto.BeforeAfterDto.builder().before(0).current(20).build())
                        .rapidAccel(CompareDto.BeforeAfterDto.builder().before(0).current(25).build())
                        .laneChange(CompareDto.BeforeAfterDto.builder().before(0).current(10).build())
                        .build())
                .build();
    }

    private CompareDto createDecreaseCompare() {
        return CompareDto.builder()
                .incdec(-25.0)
                .chart(CompareDto.ChartDto.builder()
                        .hardBrake(CompareDto.BeforeAfterDto.builder().before(40).current(30).build())
                        .rapidAccel(CompareDto.BeforeAfterDto.builder().before(45).current(35).build())
                        .laneChange(CompareDto.BeforeAfterDto.builder().before(15).current(10).build())
                        .build())
                .build();
    }

    private TotalCountsDto anyTotalCountsDto() {
        return org.mockito.ArgumentMatchers.any(TotalCountsDto.class);
    }
}