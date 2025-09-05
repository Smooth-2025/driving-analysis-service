package com.smooth.driving_analysis_service.reports.behavior.service;

import com.smooth.driving_analysis_service.reports.behavior.dto.request.BehaviorDiffRequestDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.*;
import com.smooth.driving_analysis_service.reports.behavior.dto.result.BehaviorSummaryResultDto;
import com.smooth.driving_analysis_service.reports.behavior.entity.BehaviorType;
import com.smooth.driving_analysis_service.reports.behavior.entity.TimeSlot;
import com.smooth.driving_analysis_service.reports.behavior.repository.BehaviorDrivingRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BehaviorReportService 테스트")
class BehaviorReportServiceImplTest {

    @Mock
    private BehaviorDrivingRecordRepository repo;

    @InjectMocks
    private BehaviorReportServiceImpl behaviorReportService;

    private BehaviorSummaryResultDto mockSummaryResult;

    @BeforeEach
    void setUp() {
        mockSummaryResult = BehaviorSummaryResultDto.builder()
                .hardBrakeCount(10)
                .rapidAccelCount(15)
                .laneChangeCount(8)
                .build();
    }

    @Test
    @DisplayName("위험운전 행동 분석 조회 - 성공")
    void getBehaviorAnalysis_Success() {
        // given
        String reportId = "test-report-123";

        // when
        BehaviorAnalysisResponseDto result = behaviorReportService.getBehaviorAnalysis(reportId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getReportId()).isEqualTo(reportId);
        assertThat(result.getTotalCounts()).isNotNull();
        assertThat(result.getTotalCounts().getTotal()).isEqualTo(97);
        assertThat(result.getDrivingPattern()).isNotNull();
        assertThat(result.getCompare()).isNotNull();
    }

    @Test
    @DisplayName("행동 요약 조회 - 성공")
    void getSummary_Success() {
        // given
        Long reportId = 123L;
        when(repo.fetchSummary(reportId)).thenReturn(mockSummaryResult);

        // when
        BehaviorSummaryResponseDto result = behaviorReportService.getSummary(reportId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getHardBrakeCount()).isEqualTo(10);
        assertThat(result.getRapidAccelCount()).isEqualTo(15);
        assertThat(result.getLaneChangeCount()).isEqualTo(8);
        assertThat(result.getTotal()).isEqualTo(33);
        
        verify(repo).fetchSummary(reportId);
    }

    @Test
    @DisplayName("궤적 분석 조회 - 성공")
    void getTrajectory_Success() {
        // given
        Long reportId = 123L;
        Object[][] mockData = {
                {"HARD_BRAKE", 1, "COMMUTE", 5},
                {"RAPID_ACCEL", 2, "NIGHT", 3}
        };
        when(repo.findDominantPointsByItems(reportId)).thenReturn(Arrays.asList(mockData));

        // when
        BehaviorTrajectoryResponseDto result = behaviorReportService.getTrajectory(reportId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getDominantSlots()).isNotNull();
        assertThat(result.getInsight()).contains("급제동", "급가속", "차선변경");
        
        verify(repo).findDominantPointsByItems(reportId);
    }

    @Test
    @DisplayName("행동 차이 분석 - 이전 리포트 있음")
    void getDiff_WithPreviousReport() {
        // given
        Long reportId = 123L;
        Long prevReportId = 122L;
        BehaviorDiffRequestDto request = BehaviorDiffRequestDto.builder()
                .prevReportId(prevReportId)
                .build();

        BehaviorSummaryResultDto currentResult = BehaviorSummaryResultDto.builder()
                .hardBrakeCount(12)
                .rapidAccelCount(18)
                .laneChangeCount(6)
                .build();

        BehaviorSummaryResultDto previousResult = BehaviorSummaryResultDto.builder()
                .hardBrakeCount(10)
                .rapidAccelCount(15)
                .laneChangeCount(8)
                .build();

        when(repo.fetchSummary(reportId)).thenReturn(currentResult);
        when(repo.fetchSummary(prevReportId)).thenReturn(previousResult);

        // when
        List<BehaviorDiffResponseDto> result = behaviorReportService.getDiff(reportId, request);

        // then
        assertThat(result).hasSize(3);
        
        BehaviorDiffResponseDto hardBrakeDiff = result.stream()
                .filter(r -> r.getBehavior() == BehaviorType.HARD_BRAKE)
                .findFirst().orElse(null);
        
        assertThat(hardBrakeDiff).isNotNull();
        assertThat(hardBrakeDiff.getPrev()).isEqualTo(10);
        assertThat(hardBrakeDiff.getCurr()).isEqualTo(12);
        assertThat(hardBrakeDiff.getDiff()).isEqualTo(2);
        assertThat(hardBrakeDiff.getDirection()).isEqualTo(BehaviorDiffResponseDto.Direction.INCREASE);
        
        verify(repo).fetchSummary(reportId);
        verify(repo).fetchSummary(prevReportId);
    }

    @Test
    @DisplayName("행동 차이 분석 - 이전 리포트 없음")
    void getDiff_WithoutPreviousReport() {
        // given
        Long reportId = 123L;
        when(repo.fetchSummary(reportId)).thenReturn(mockSummaryResult);

        // when
        List<BehaviorDiffResponseDto> result = behaviorReportService.getDiff(reportId, null);

        // then
        assertThat(result).hasSize(3);
        
        BehaviorDiffResponseDto hardBrakeDiff = result.stream()
                .filter(r -> r.getBehavior() == BehaviorType.HARD_BRAKE)
                .findFirst().orElse(null);
        
        assertThat(hardBrakeDiff).isNotNull();
        assertThat(hardBrakeDiff.getPrev()).isEqualTo(0);
        assertThat(hardBrakeDiff.getCurr()).isEqualTo(10);
        assertThat(hardBrakeDiff.getDiff()).isEqualTo(10);
        
        verify(repo).fetchSummary(reportId);
    }

    @Test
    @DisplayName("행동 코멘트 조회 - 성공")
    void getComment_Success() {
        // given
        Long reportId = 123L;
        Long prevReportId = 122L;
        BehaviorDiffRequestDto request = BehaviorDiffRequestDto.builder()
                .prevReportId(prevReportId)
                .build();

        BehaviorSummaryResultDto currentResult = BehaviorSummaryResultDto.builder()
                .hardBrakeCount(12)
                .rapidAccelCount(10)
                .laneChangeCount(8)
                .build();

        BehaviorSummaryResultDto previousResult = BehaviorSummaryResultDto.builder()
                .hardBrakeCount(10)
                .rapidAccelCount(15)
                .laneChangeCount(8)
                .build();

        when(repo.fetchSummary(reportId)).thenReturn(currentResult);
        when(repo.fetchSummary(prevReportId)).thenReturn(previousResult);

        // when
        BehaviorCommentResponseDto result = behaviorReportService.getComment(reportId, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getText()).contains("급제동", "급가속", "차선변경");
        assertThat(result.getText()).contains("▲", "▼", "▬");
        
        verify(repo).fetchSummary(reportId);
        verify(repo).fetchSummary(prevReportId);
    }

    @Test
    @DisplayName("중간 스냅샷 생성/갱신 - 성공")
    void createOrUpdateInterimSnapshot_Success() {
        // given
        Long reportId = 123L;

        // when & then
        behaviorReportService.createOrUpdateInterimSnapshot(reportId);
        // TODO 구현 후 검증 로직 추가
    }

    @Test
    @DisplayName("최종 스냅샷 생성 - 성공")
    void createFinalSnapshot_Success() {
        // given
        Long reportId = 123L;

        // when & then
        behaviorReportService.createFinalSnapshot(reportId);
        // TODO 구현 후 검증 로직 추가
    }
}