package com.smooth.driving_analysis_service.reports.behavior.service;

import com.smooth.driving_analysis_service.reports.behavior.dto.response.BehaviorAnalysisResponseDto;
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
@DisplayName("BehaviorReportService Task 1 테스트")
class BehaviorReportServiceImplTask1Test {

    @Mock
    private BehaviorTotalCountsRepository totalCountsRepository;

    @InjectMocks
    private BehaviorReportServiceImpl behaviorReportService;

    @Test
    @DisplayName("Task 1: totalCounts 정상 조회")
    void getBehaviorAnalysis_Success() {
        // given
        String reportId = "u1_r3_20250901";
        TotalCountsDto mockTotalCounts = TotalCountsDto.of(38, 42, 17);
        
        when(totalCountsRepository.findTotalCountsByReportId(3L))
                .thenReturn(mockTotalCounts);

        // when
        BehaviorAnalysisResponseDto result = behaviorReportService.getBehaviorAnalysis(reportId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getReportId()).isEqualTo(reportId);
        assertThat(result.getTotalCounts()).isNotNull();
        assertThat(result.getTotalCounts().getHardBrake()).isEqualTo(38);
        assertThat(result.getTotalCounts().getRapidAccel()).isEqualTo(42);
        assertThat(result.getTotalCounts().getLaneChange()).isEqualTo(17);
        assertThat(result.getTotalCounts().getTotal()).isEqualTo(97);
    }

    @Test
    @DisplayName("Task 1: 데이터 없을 때 기본값 반환")
    void getBehaviorAnalysis_NoData() {
        // given
        String reportId = "u1_r999_20250901";
        
        when(totalCountsRepository.findTotalCountsByReportId(anyLong()))
                .thenReturn(null);

        // when
        BehaviorAnalysisResponseDto result = behaviorReportService.getBehaviorAnalysis(reportId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getReportId()).isEqualTo(reportId);
        assertThat(result.getTotalCounts()).isNotNull();
        assertThat(result.getTotalCounts().getHardBrake()).isEqualTo(0);
        assertThat(result.getTotalCounts().getRapidAccel()).isEqualTo(0);
        assertThat(result.getTotalCounts().getLaneChange()).isEqualTo(0);
        assertThat(result.getTotalCounts().getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Task 1: reportId 파싱 오류시 기본값 사용")
    void getBehaviorAnalysis_InvalidReportId() {
        // given
        String reportId = "invalid_format";
        TotalCountsDto mockTotalCounts = TotalCountsDto.of(5, 3, 2);
        
        when(totalCountsRepository.findTotalCountsByReportId(1L)) // 기본값 1L 사용
                .thenReturn(mockTotalCounts);

        // when
        BehaviorAnalysisResponseDto result = behaviorReportService.getBehaviorAnalysis(reportId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getReportId()).isEqualTo(reportId);
        assertThat(result.getTotalCounts().getTotal()).isEqualTo(10);
    }
}