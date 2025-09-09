package com.smooth.driving_analysis_service.reports.behavior.service;

import com.smooth.driving_analysis_service.reports.behavior.dto.projection.EventPatternProjectionDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.BehaviorAnalysisResponseDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.CompareDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.DrivingPatternDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.TotalCountsDto;
import com.smooth.driving_analysis_service.reports.behavior.repository.BehaviorPatternRepository;
import com.smooth.driving_analysis_service.reports.behavior.repository.BehaviorTotalCountsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BehaviorReportService 단위 테스트")
class BehaviorReportServiceImplTest {

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

    private TotalCountsDto mockTotalCounts;
    private DrivingPatternDto mockDrivingPattern;
    private CompareDto mockCompare;

    @BeforeEach
    void setUp() {
        mockTotalCounts = TotalCountsDto.of(38, 42, 17);
        
        mockDrivingPattern = DrivingPatternDto.builder()
                .weekday("금요일")
                .timeslot("저녁")
                .chart(List.of())
                .comment("평일 저녁에는 급제동과 급가속이 늘어나는 패턴이 보여요!")
                .build();

        mockCompare = CompareDto.builder()
                .incdec(1.04)
                .chart(CompareDto.ChartDto.builder()
                        .hardBrake(CompareDto.BeforeAfterDto.builder().before(35).current(38).build())
                        .rapidAccel(CompareDto.BeforeAfterDto.builder().before(40).current(42).build())
                        .laneChange(CompareDto.BeforeAfterDto.builder().before(15).current(17).build())
                        .build())
                .build();
    }

    @Test
    @DisplayName("Task 1: calculateTotalCounts - 정상 케이스")
    void calculateTotalCounts_Success() {
        // given
        List<String> drivingIds = Arrays.asList("driving-001", "driving-002", "driving-003");
        when(totalCountsRepository.findTotalCountsByDrivingIds(drivingIds))
                .thenReturn(mockTotalCounts);

        // when
        TotalCountsDto result = behaviorReportService.calculateTotalCounts(drivingIds);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getHardBrake()).isEqualTo(38);
        assertThat(result.getRapidAccel()).isEqualTo(42);
        assertThat(result.getLaneChange()).isEqualTo(17);
        assertThat(result.getTotal()).isEqualTo(97);
    }

    @Test
    @DisplayName("Task 1: calculateTotalCounts - 예외 발생시 기본값 반환")
    void calculateTotalCounts_Exception_ReturnsDefault() {
        // given
        List<String> drivingIds = Arrays.asList("driving-001");
        when(totalCountsRepository.findTotalCountsByDrivingIds(drivingIds))
                .thenThrow(new RuntimeException("Database error"));

        // when
        TotalCountsDto result = behaviorReportService.calculateTotalCounts(drivingIds);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getHardBrake()).isEqualTo(0);
        assertThat(result.getRapidAccel()).isEqualTo(0);
        assertThat(result.getLaneChange()).isEqualTo(0);
        assertThat(result.getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Task 2: analyzeDrivingPattern - 정상 케이스")
    void analyzeDrivingPattern_Success() {
        // given
        List<String> drivingIds = Arrays.asList("driving-001", "driving-002");
        List<EventPatternProjectionDto> mockProjections = List.of();
        
        when(behaviorPatternRepository.findEventPatternsByDrivingIds(drivingIds))
                .thenReturn(mockProjections);
        when(patternAnalyzer.analyzeDrivingPattern(mockProjections))
                .thenReturn(mockDrivingPattern);

        // when
        DrivingPatternDto result = behaviorReportService.analyzeDrivingPattern(drivingIds);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getWeekday()).isEqualTo("금요일");
        assertThat(result.getTimeslot()).isEqualTo("저녁");
        assertThat(result.getComment()).contains("평일 저녁");
    }

    @Test
    @DisplayName("Task 2: analyzeDrivingPattern - 예외 발생시 기본값 반환")
    void analyzeDrivingPattern_Exception_ReturnsDefault() {
        // given
        List<String> drivingIds = Arrays.asList("driving-001");
        when(behaviorPatternRepository.findEventPatternsByDrivingIds(drivingIds))
                .thenThrow(new RuntimeException("Athena query failed"));

        // when
        DrivingPatternDto result = behaviorReportService.analyzeDrivingPattern(drivingIds);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getWeekday()).isEqualTo("금요일");
        assertThat(result.getTimeslot()).isEqualTo("저녁");
        assertThat(result.getComment()).contains("오류가 발생했습니다");
    }

    @Test
    @DisplayName("Task 3: compareWithPrevious - 정상 케이스")
    void compareWithPrevious_Success() {
        // given
        String reportId = "u1_r3_20250901";
        TotalCountsDto currentCounts = TotalCountsDto.of(38, 42, 17);
        
        when(compareAnalyzer.analyzeCompare(3L, currentCounts))
                .thenReturn(mockCompare);

        // when
        CompareDto result = behaviorReportService.compareWithPrevious(reportId, currentCounts);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getIncdec()).isEqualTo(1.04);
        assertThat(result.getChart().getHardBrake().getBefore()).isEqualTo(35);
        assertThat(result.getChart().getHardBrake().getCurrent()).isEqualTo(38);
    }

    @Test
    @DisplayName("Task 3: compareWithPrevious - 예외 발생시 기본값 반환")
    void compareWithPrevious_Exception_ReturnsDefault() {
        // given
        String reportId = "u1_r3_20250901";
        TotalCountsDto currentCounts = TotalCountsDto.of(38, 42, 17);
        
        when(compareAnalyzer.analyzeCompare(anyLong(), any(TotalCountsDto.class)))
                .thenThrow(new RuntimeException("Compare analysis failed"));

        // when
        CompareDto result = behaviorReportService.compareWithPrevious(reportId, currentCounts);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getIncdec()).isEqualTo(0.0);
        assertThat(result.getChart().getHardBrake().getBefore()).isEqualTo(0);
        assertThat(result.getChart().getHardBrake().getCurrent()).isEqualTo(38);
    }

    @Test
    @DisplayName("getBehaviorAnalysis - 전체 통합 테스트")
    void getBehaviorAnalysis_Success() {
        // given
        String reportId = "u1_r3_20250901";
        
        when(totalCountsRepository.findTotalCountsByReportId(3L))
                .thenReturn(mockTotalCounts);
        when(behaviorPatternRepository.findEventPatternsByReportId(3L))
                .thenReturn(List.of());
        when(patternAnalyzer.analyzeDrivingPattern(anyList()))
                .thenReturn(mockDrivingPattern);
        when(compareAnalyzer.analyzeCompare(anyLong(), any(TotalCountsDto.class)))
                .thenReturn(mockCompare);

        // when
        BehaviorAnalysisResponseDto result = behaviorReportService.getBehaviorAnalysis(reportId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getReportId()).isEqualTo(reportId);
        
        // totalCounts 검증
        assertThat(result.getTotalCounts().getHardBrake()).isEqualTo(38);
        assertThat(result.getTotalCounts().getRapidAccel()).isEqualTo(42);
        assertThat(result.getTotalCounts().getLaneChange()).isEqualTo(17);
        assertThat(result.getTotalCounts().getTotal()).isEqualTo(97);
        
        // drivingPattern 검증
        assertThat(result.getDrivingPattern().getWeekday()).isEqualTo("금요일");
        assertThat(result.getDrivingPattern().getTimeslot()).isEqualTo("저녁");
        
        // compare 검증
        assertThat(result.getCompare().getIncdec()).isEqualTo(1.04);
    }

    @Test
    @DisplayName("reportId 파싱 테스트 - 다양한 형식")
    void getBehaviorAnalysis_ReportIdParsing() {
        // given
        when(totalCountsRepository.findTotalCountsByReportId(anyLong()))
                .thenReturn(TotalCountsDto.of(0, 0, 0));
        when(behaviorPatternRepository.findEventPatternsByReportId(anyLong()))
                .thenReturn(List.of());
        when(patternAnalyzer.analyzeDrivingPattern(anyList()))
                .thenReturn(mockDrivingPattern);
        when(compareAnalyzer.analyzeCompare(anyLong(), any(TotalCountsDto.class)))
                .thenReturn(mockCompare);

        // when & then - 정상적인 reportId
        BehaviorAnalysisResponseDto result1 = behaviorReportService.getBehaviorAnalysis("u1_r5_20250901");
        assertThat(result1.getReportId()).isEqualTo("u1_r5_20250901");

        // when & then - 비정상적인 reportId (기본값 1L 사용)
        BehaviorAnalysisResponseDto result2 = behaviorReportService.getBehaviorAnalysis("invalid_format");
        assertThat(result2.getReportId()).isEqualTo("invalid_format");
    }
}