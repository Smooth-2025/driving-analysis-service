package com.smooth.driving_analysis_service.reports.behavior.service;

import com.smooth.driving_analysis_service.reports.behavior.dto.projection.EventPatternProjection;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.BehaviorAnalysisResponseDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.CompareDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.DrivingPatternDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.TotalCountsDto;
import com.smooth.driving_analysis_service.reports.behavior.repository.BehaviorPatternRepository;
import com.smooth.driving_analysis_service.reports.behavior.repository.BehaviorTotalCountsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BehaviorReportService 단위 테스트")
class BehaviorReportServiceTest {

    @Mock
    private BehaviorTotalCountsRepository totalCountsRepository;
    
    @Mock
    private BehaviorPatternRepository behaviorPatternRepository;
    
    @Mock
    private BehaviorPatternAnalyzer patternAnalyzer;
    
    @Mock
    private BehaviorCompareAnalyzer compareAnalyzer;

    @InjectMocks
    private BehaviorReportServiceImpl behaviorReportService;

    @Test
    @DisplayName("정상적인 리포트 ID로 전체 분석 데이터 조회")
    void getBehaviorAnalysis_Success() {
        // given
        String reportId = "u1_r3_20250901";
        TotalCountsDto mockTotalCounts = TotalCountsDto.of(38, 42, 17);
        DrivingPatternDto mockPattern = createMockDrivingPattern();
        CompareDto mockCompare = createMockCompare();
        
        when(totalCountsRepository.findTotalCountsByReportId(3L))
                .thenReturn(mockTotalCounts);
        when(behaviorPatternRepository.findEventPatternsByReportId(3L))
                .thenReturn(List.of());
        when(patternAnalyzer.analyzeDrivingPattern(any()))
                .thenReturn(mockPattern);
        when(compareAnalyzer.analyzeCompare(anyLong(), any(TotalCountsDto.class)))
                .thenReturn(mockCompare);

        // when
        BehaviorAnalysisResponseDto result = behaviorReportService.getBehaviorAnalysis(reportId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getReportId()).isEqualTo(reportId);
        
        // totalCounts 검증
        assertThat(result.getTotalCounts()).isNotNull();
        assertThat(result.getTotalCounts().getHardBrake()).isEqualTo(38);
        assertThat(result.getTotalCounts().getRapidAccel()).isEqualTo(42);
        assertThat(result.getTotalCounts().getLaneChange()).isEqualTo(17);
        assertThat(result.getTotalCounts().getTotal()).isEqualTo(97);
        
        // drivingPattern 검증
        assertThat(result.getDrivingPattern()).isNotNull();
        assertThat(result.getDrivingPattern().getWeekday()).isEqualTo("금요일");
        assertThat(result.getDrivingPattern().getTimeslot()).isEqualTo("저녁");
        
        // compare 검증
        assertThat(result.getCompare()).isNotNull();
        assertThat(result.getCompare().getIncdec()).isEqualTo(7.5);
    }

    @Test
    @DisplayName("데이터가 없을 때 기본값 반환")
    void getBehaviorAnalysis_NoData() {
        // given
        String reportId = "u1_r999_20250901";
        
        when(totalCountsRepository.findTotalCountsByReportId(anyLong()))
                .thenReturn(null);
        when(behaviorPatternRepository.findEventPatternsByReportId(anyLong()))
                .thenReturn(List.of());
        when(patternAnalyzer.analyzeDrivingPattern(any()))
                .thenReturn(createEmptyDrivingPattern());
        when(compareAnalyzer.analyzeCompare(anyLong(), any(TotalCountsDto.class)))
                .thenReturn(createEmptyCompare());

        // when
        BehaviorAnalysisResponseDto result = behaviorReportService.getBehaviorAnalysis(reportId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getReportId()).isEqualTo(reportId);
        assertThat(result.getTotalCounts().getTotal()).isEqualTo(0);
        assertThat(result.getDrivingPattern()).isNotNull();
        assertThat(result.getCompare()).isNotNull();
    }

    @Test
    @DisplayName("잘못된 리포트 ID 형식일 때 기본값 사용")
    void getBehaviorAnalysis_InvalidReportId() {
        // given
        String reportId = "invalid_format";
        TotalCountsDto mockTotalCounts = TotalCountsDto.of(5, 3, 2);
        
        when(totalCountsRepository.findTotalCountsByReportId(1L)) // 기본값 1L 사용
                .thenReturn(mockTotalCounts);
        when(behaviorPatternRepository.findEventPatternsByReportId(1L))
                .thenReturn(List.of());
        when(patternAnalyzer.analyzeDrivingPattern(any()))
                .thenReturn(createMockDrivingPattern());
        when(compareAnalyzer.analyzeCompare(anyLong(), any(TotalCountsDto.class)))
                .thenReturn(createMockCompare());

        // when
        BehaviorAnalysisResponseDto result = behaviorReportService.getBehaviorAnalysis(reportId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getReportId()).isEqualTo(reportId);
        assertThat(result.getTotalCounts().getTotal()).isEqualTo(10);
    }

    @Test
    @DisplayName("Repository 예외 발생시 기본 응답 반환")
    void getBehaviorAnalysis_RepositoryException() {
        // given
        String reportId = "u1_r1_20250901";
        
        when(totalCountsRepository.findTotalCountsByReportId(anyLong()))
                .thenThrow(new RuntimeException("Database error"));

        // when
        BehaviorAnalysisResponseDto result = behaviorReportService.getBehaviorAnalysis(reportId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getReportId()).isEqualTo(reportId);
        assertThat(result.getTotalCounts().getTotal()).isEqualTo(0);
        assertThat(result.getDrivingPattern().getComment()).contains("오류가 발생했습니다");
        assertThat(result.getCompare().getIncdec()).isEqualTo(0.0);
    }

    private DrivingPatternDto createMockDrivingPattern() {
        return DrivingPatternDto.builder()
                .weekday("금요일")
                .timeslot("저녁")
                .chart(List.of())
                .comment("평일 저녁에는 급제동과 급가속이 늘어나는 패턴이 보여요!")
                .build();
    }

    private DrivingPatternDto createEmptyDrivingPattern() {
        return DrivingPatternDto.builder()
                .weekday("금요일")
                .timeslot("저녁")
                .chart(List.of())
                .comment("아직 충분한 주행 데이터가 없어 패턴을 분석할 수 없습니다.")
                .build();
    }

    private CompareDto createMockCompare() {
        return CompareDto.builder()
                .incdec(7.5)
                .chart(CompareDto.ChartDto.builder()
                        .hardBrake(CompareDto.BeforeAfterDto.builder().before(35).current(38).build())
                        .rapidAccel(CompareDto.BeforeAfterDto.builder().before(40).current(42).build())
                        .laneChange(CompareDto.BeforeAfterDto.builder().before(15).current(17).build())
                        .build())
                .build();
    }

    private CompareDto createEmptyCompare() {
        return CompareDto.builder()
                .incdec(0.0)
                .chart(CompareDto.ChartDto.builder()
                        .hardBrake(CompareDto.BeforeAfterDto.builder().before(0).current(0).build())
                        .rapidAccel(CompareDto.BeforeAfterDto.builder().before(0).current(0).build())
                        .laneChange(CompareDto.BeforeAfterDto.builder().before(0).current(0).build())
                        .build())
                .build();
    }
}