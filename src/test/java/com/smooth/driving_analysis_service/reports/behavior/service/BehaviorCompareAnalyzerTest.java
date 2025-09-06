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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BehaviorCompareAnalyzer 테스트")
class BehaviorCompareAnalyzerTest {

    @Mock
    private BehaviorTotalCountsRepository totalCountsRepository;

    @InjectMocks
    private BehaviorCompareAnalyzer compareAnalyzer;

    @Test
    @DisplayName("정상적인 증감 분석 - 증가 케이스")
    void analyzeCompare_Increase_Success() {
        // given
        Long currentReportId = 3L;
        TotalCountsDto currentCounts = TotalCountsDto.of(38, 42, 17); // total: 97
        TotalCountsDto previousCounts = TotalCountsDto.of(35, 40, 15); // total: 90
        
        when(totalCountsRepository.findTotalCountsByReportId(2L)) // currentReportId - 1
                .thenReturn(previousCounts);

        // when
        CompareDto result = compareAnalyzer.analyzeCompare(currentReportId, currentCounts);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getIncdec()).isEqualTo(7.78); // (97-90)/90*100 ≈ 7.78%
        
        assertThat(result.getChart()).isNotNull();
        assertThat(result.getChart().getHardBrake().getBefore()).isEqualTo(35);
        assertThat(result.getChart().getHardBrake().getCurrent()).isEqualTo(38);
        assertThat(result.getChart().getRapidAccel().getBefore()).isEqualTo(40);
        assertThat(result.getChart().getRapidAccel().getCurrent()).isEqualTo(42);
        assertThat(result.getChart().getLaneChange().getBefore()).isEqualTo(15);
        assertThat(result.getChart().getLaneChange().getCurrent()).isEqualTo(17);
    }

    @Test
    @DisplayName("정상적인 증감 분석 - 감소 케이스")
    void analyzeCompare_Decrease_Success() {
        // given
        Long currentReportId = 3L;
        TotalCountsDto currentCounts = TotalCountsDto.of(30, 35, 10); // total: 75
        TotalCountsDto previousCounts = TotalCountsDto.of(40, 45, 15); // total: 100
        
        when(totalCountsRepository.findTotalCountsByReportId(2L))
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
    @DisplayName("이전 리포트 데이터 없을 때 기본값 처리")
    void analyzeCompare_NoPreviousData() {
        // given
        Long currentReportId = 1L; // 첫 번째 리포트
        TotalCountsDto currentCounts = TotalCountsDto.of(20, 25, 10); // total: 55
        
        when(totalCountsRepository.findTotalCountsByReportId(0L))
                .thenReturn(null); // 이전 데이터 없음

        // when
        CompareDto result = compareAnalyzer.analyzeCompare(currentReportId, currentCounts);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getIncdec()).isEqualTo(100.0); // 이전이 0이면 100% 증가
        
        assertThat(result.getChart().getHardBrake().getBefore()).isEqualTo(0);
        assertThat(result.getChart().getHardBrake().getCurrent()).isEqualTo(20);
        assertThat(result.getChart().getRapidAccel().getBefore()).isEqualTo(0);
        assertThat(result.getChart().getRapidAccel().getCurrent()).isEqualTo(25);
        assertThat(result.getChart().getLaneChange().getBefore()).isEqualTo(0);
        assertThat(result.getChart().getLaneChange().getCurrent()).isEqualTo(10);
    }

    @Test
    @DisplayName("이전 리포트가 0일 때 처리")
    void analyzeCompare_PreviousZero() {
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
        assertThat(result.getIncdec()).isEqualTo(100.0); // 0에서 30으로 증가 = 100%
    }

    @Test
    @DisplayName("현재와 이전이 동일할 때")
    void analyzeCompare_NoChange() {
        // given
        Long currentReportId = 3L;
        TotalCountsDto currentCounts = TotalCountsDto.of(20, 25, 10); // total: 55
        TotalCountsDto previousCounts = TotalCountsDto.of(20, 25, 10); // total: 55
        
        when(totalCountsRepository.findTotalCountsByReportId(2L))
                .thenReturn(previousCounts);

        // when
        CompareDto result = compareAnalyzer.analyzeCompare(currentReportId, currentCounts);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getIncdec()).isEqualTo(0.0); // 변화 없음
    }

    @Test
    @DisplayName("예외 발생시 기본값 반환")
    void analyzeCompare_ExceptionHandling() {
        // given
        Long currentReportId = 3L;
        TotalCountsDto currentCounts = TotalCountsDto.of(20, 25, 10);
        
        when(totalCountsRepository.findTotalCountsByReportId(anyLong()))
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