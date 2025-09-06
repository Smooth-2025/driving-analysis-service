package com.smooth.driving_analysis_service.reports.behavior.service;

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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BehaviorCompareAnalyzer 단위 테스트")
class BehaviorCompareAnalyzerTest {

    @Mock
    private BehaviorTotalCountsRepository totalCountsRepository;

    @InjectMocks
    private BehaviorCompareAnalyzer compareAnalyzer;

    @Test
    @DisplayName("정상적인 증가 패턴 분석")
    void analyzeCompare_IncreasePattern() {
        // given
        Long currentReportId = 3L;
        TotalCountsDto currentCounts = TotalCountsDto.of(38, 42, 17); // total: 97
        TotalCountsDto previousCounts = TotalCountsDto.of(35, 40, 15); // total: 90
        
        when(totalCountsRepository.findTotalCountsByReportId(2L))
                .thenReturn(previousCounts);

        // when
        CompareDto result = compareAnalyzer.analyzeCompare(currentReportId, currentCounts);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getIncdec()).isEqualTo(7.78); // (97-90)/90*100 = 7.78%
        
        assertThat(result.getChart().getHardBrake().getBefore()).isEqualTo(35);
        assertThat(result.getChart().getHardBrake().getCurrent()).isEqualTo(38);
        
        assertThat(result.getChart().getRapidAccel().getBefore()).isEqualTo(40);
        assertThat(result.getChart().getRapidAccel().getCurrent()).isEqualTo(42);
        
        assertThat(result.getChart().getLaneChange().getBefore()).isEqualTo(15);
        assertThat(result.getChart().getLaneChange().getCurrent()).isEqualTo(17);
    }

    @Test
    @DisplayName("감소 패턴 분석")
    void analyzeCompare_DecreasePattern() {
        // given
        Long currentReportId = 4L;
        TotalCountsDto currentCounts = TotalCountsDto.of(30, 35, 10); // total: 75
        TotalCountsDto previousCounts = TotalCountsDto.of(40, 45, 15); // total: 100
        
        when(totalCountsRepository.findTotalCountsByReportId(3L))
                .thenReturn(previousCounts);

        // when
        CompareDto result = compareAnalyzer.analyzeCompare(currentReportId, currentCounts);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getIncdec()).isEqualTo(-25.0); // (75-100)/100*100 = -25%
        
        assertThat(result.getChart().getHardBrake().getBefore()).isEqualTo(40);
        assertThat(result.getChart().getHardBrake().getCurrent()).isEqualTo(30);
    }

    @Test
    @DisplayName("첫 번째 리포트 (이전 데이터 없음)")
    void analyzeCompare_FirstReport() {
        // given
        Long currentReportId = 1L;
        TotalCountsDto currentCounts = TotalCountsDto.of(20, 25, 10); // total: 55
        
        when(totalCountsRepository.findTotalCountsByReportId(0L))
                .thenReturn(null);

        // when
        CompareDto result = compareAnalyzer.analyzeCompare(currentReportId, currentCounts);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getIncdec()).isEqualTo(100.0); // 첫 리포트는 100% 증가
        
        assertThat(result.getChart().getHardBrake().getBefore()).isEqualTo(0);
        assertThat(result.getChart().getHardBrake().getCurrent()).isEqualTo(20);
    }

    @Test
    @DisplayName("이전 데이터가 0일 때")
    void analyzeCompare_PreviousDataIsZero() {
        // given
        Long currentReportId = 2L;
        TotalCountsDto currentCounts = TotalCountsDto.of(10, 15, 5); // total: 30
        TotalCountsDto previousCounts = TotalCountsDto.of(0, 0, 0); // total: 0
        
        when(totalCountsRepository.findTotalCountsByReportId(1L))
                .thenReturn(previousCounts);

        // when
        CompareDto result = compareAnalyzer.analyzeCompare(currentReportId, currentCounts);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getIncdec()).isEqualTo(100.0); // 0에서 증가하면 100%
    }

    @Test
    @DisplayName("현재 데이터가 0일 때")
    void analyzeCompare_CurrentDataIsZero() {
        // given
        Long currentReportId = 3L;
        TotalCountsDto currentCounts = TotalCountsDto.of(0, 0, 0); // total: 0
        TotalCountsDto previousCounts = TotalCountsDto.of(10, 15, 5); // total: 30
        
        when(totalCountsRepository.findTotalCountsByReportId(2L))
                .thenReturn(previousCounts);

        // when
        CompareDto result = compareAnalyzer.analyzeCompare(currentReportId, currentCounts);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getIncdec()).isEqualTo(-100.0); // 100% 감소
    }

    @Test
    @DisplayName("Repository 예외 발생시 기본값 반환")
    void analyzeCompare_RepositoryException() {
        // given
        Long currentReportId = 3L;
        TotalCountsDto currentCounts = TotalCountsDto.of(20, 25, 10);
        
        when(totalCountsRepository.findTotalCountsByReportId(2L))
                .thenThrow(new RuntimeException("Database error"));

        // when
        CompareDto result = compareAnalyzer.analyzeCompare(currentReportId, currentCounts);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getIncdec()).isEqualTo(100.0); // 기본값
        assertThat(result.getChart().getHardBrake().getBefore()).isEqualTo(0);
        assertThat(result.getChart().getHardBrake().getCurrent()).isEqualTo(20);
    }
}