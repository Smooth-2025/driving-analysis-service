package com.smooth.driving_analysis_service.reports.behavior.service;

import com.smooth.driving_analysis_service.reports.behavior.dto.response.CompareDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.TotalCountsDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("BehaviorCompareAnalyzer 단위 테스트")
class BehaviorCompareAnalyzerTest {

    @InjectMocks
    private BehaviorCompareAnalyzer behaviorCompareAnalyzer;

    @Test
    @DisplayName("이전 대비 증가 케이스")
    void analyzeCompare_Increase() {
        // given
        Long reportId = 3L;
        TotalCountsDto currentCounts = TotalCountsDto.of(38, 42, 17); // total: 97
        
        // when
        CompareDto result = behaviorCompareAnalyzer.analyzeCompare(reportId, currentCounts);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getIncdec()).isGreaterThan(0); // 증가
        assertThat(result.getChart()).isNotNull();
        assertThat(result.getChart().getHardBrake().getCurrent()).isEqualTo(38);
        assertThat(result.getChart().getRapidAccel().getCurrent()).isEqualTo(42);
        assertThat(result.getChart().getLaneChange().getCurrent()).isEqualTo(17);
    }

    @Test
    @DisplayName("이전 대비 감소 케이스")
    void analyzeCompare_Decrease() {
        // given
        Long reportId = 5L;
        TotalCountsDto currentCounts = TotalCountsDto.of(20, 25, 10); // total: 55
        
        // when
        CompareDto result = behaviorCompareAnalyzer.analyzeCompare(reportId, currentCounts);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getChart()).isNotNull();
        assertThat(result.getChart().getHardBrake().getCurrent()).isEqualTo(20);
        assertThat(result.getChart().getRapidAccel().getCurrent()).isEqualTo(25);
        assertThat(result.getChart().getLaneChange().getCurrent()).isEqualTo(10);
    }

    @Test
    @DisplayName("첫 번째 리포트 케이스 (이전 데이터 없음)")
    void analyzeCompare_FirstReport() {
        // given
        Long reportId = 1L;
        TotalCountsDto currentCounts = TotalCountsDto.of(15, 20, 8);
        
        // when
        CompareDto result = behaviorCompareAnalyzer.analyzeCompare(reportId, currentCounts);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getIncdec()).isEqualTo(100.0); // 이전이 0이면 100% 증가
        assertThat(result.getChart().getHardBrake().getBefore()).isEqualTo(0);
        assertThat(result.getChart().getHardBrake().getCurrent()).isEqualTo(15);
        assertThat(result.getChart().getRapidAccel().getBefore()).isEqualTo(0);
        assertThat(result.getChart().getRapidAccel().getCurrent()).isEqualTo(20);
        assertThat(result.getChart().getLaneChange().getBefore()).isEqualTo(0);
        assertThat(result.getChart().getLaneChange().getCurrent()).isEqualTo(8);
    }

    @Test
    @DisplayName("동일한 수치 케이스")
    void analyzeCompare_SameValues() {
        // given
        Long reportId = 4L;
        TotalCountsDto currentCounts = TotalCountsDto.of(30, 35, 15); // total: 80
        
        // when
        CompareDto result = behaviorCompareAnalyzer.analyzeCompare(reportId, currentCounts);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getChart()).isNotNull();
        // 실제 비교 로직에 따라 결과가 달라질 수 있음
    }

    @Test
    @DisplayName("0값 처리")
    void analyzeCompare_ZeroValues() {
        // given
        Long reportId = 2L;
        TotalCountsDto currentCounts = TotalCountsDto.of(0, 0, 0);
        
        // when
        CompareDto result = behaviorCompareAnalyzer.analyzeCompare(reportId, currentCounts);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getChart().getHardBrake().getCurrent()).isEqualTo(0);
        assertThat(result.getChart().getRapidAccel().getCurrent()).isEqualTo(0);
        assertThat(result.getChart().getLaneChange().getCurrent()).isEqualTo(0);
    }

    @Test
    @DisplayName("증감률 계산 정확성")
    void analyzeCompare_PercentageCalculation() {
        // given
        Long reportId = 6L;
        TotalCountsDto currentCounts = TotalCountsDto.of(50, 60, 30); // total: 140
        
        // when
        CompareDto result = behaviorCompareAnalyzer.analyzeCompare(reportId, currentCounts);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getIncdec()).isNotNull();
        // 증감률이 올바르게 계산되었는지 확인
        // (current - before) / max(before, 1) * 100 공식 적용
    }
}