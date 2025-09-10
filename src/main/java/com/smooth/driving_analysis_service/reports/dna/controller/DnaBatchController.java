// reports/dna/controller/DnaBatchController.java
package com.smooth.driving_analysis_service.reports.dna.controller;

import com.smooth.driving_analysis_service.global.common.ApiResponse;
import com.smooth.driving_analysis_service.reports.common.service.MockDataService;
import com.smooth.driving_analysis_service.reports.dna.dto.response.DnaAnalysisResponseDto;
import com.smooth.driving_analysis_service.reports.dna.entity.DnaSnapshot;
import com.smooth.driving_analysis_service.reports.dna.service.DnaBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/driving-analysis/reports")
public class DnaBatchController {

    private final DnaBatchService service;
    private final MockDataService mockDataService;

    @PostMapping("/{reportId}/dna:interim")
    public ResponseEntity<ApiResponse<DnaSnapshot>> runInterim(@PathVariable Long reportId) {
        DnaSnapshot s = service.runInterim(reportId);
        return ResponseEntity.ok(ApiResponse.success("DNA 운전성향 INTERIM 분석이 성공했습니다.",s));
    }

    @PostMapping("/{reportId}/dna:final")
    public ResponseEntity<ApiResponse<DnaSnapshot>> runFinal(@PathVariable Long reportId) {
        DnaSnapshot s = service.runFinal(reportId);
        return ResponseEntity.ok(ApiResponse.success("DNA 운전성향 FINAL 분석이 성공했습니다.",s));
    }

    /**
     * DNA 목데이터 조회 API
     */
    @GetMapping("/{reportId}/dna/mock")
    public ResponseEntity<ApiResponse<DnaAnalysisResponseDto>> getDnaMockData(@PathVariable String reportId) {
        DnaAnalysisResponseDto mockData = DnaAnalysisResponseDto.builder()
                .reportId(reportId)
                .headline("안전한 운전자")
                .radar(DnaAnalysisResponseDto.RadarDto.builder()
                        .A(65)  // 출발 성향
                        .B(75)  // 감속 성향
                        .C(55)  // 차선 변경 성향
                        .D(80)  // 사고 대응 성향
                        .build())
                .axes(Arrays.asList(
                        DnaAnalysisResponseDto.AxisDto.builder()
                                .id("A")
                                .label("A2")
                                .summary("일반형 출발: 무리하지 않는 적절한 가속이에요.")
                                .build(),
                        DnaAnalysisResponseDto.AxisDto.builder()
                                .id("B")
                                .label("B2")
                                .summary("안전형 감속: 여유있는 브레이킹으로 안전해요.")
                                .build(),
                        DnaAnalysisResponseDto.AxisDto.builder()
                                .id("C")
                                .label("C1")
                                .summary("신중형 차선변경: 필요할 때만 조심스럽게 변경해요.")
                                .build(),
                        DnaAnalysisResponseDto.AxisDto.builder()
                                .id("D")
                                .label("D3")
                                .summary("빠른 사고대응: 위험상황에 신속하게 반응해요.")
                                .build()
                ))
                .build();

        return ResponseEntity.ok(ApiResponse.success("DNA 목데이터 조회 완료", mockData));
    }

    /**
     * DNA JSON 파일 목데이터 조회 API
     */
    @GetMapping("/{reportId}/dna/json")
    public ResponseEntity<ApiResponse<DnaAnalysisResponseDto>> getDnaJsonMockData(@PathVariable String reportId) {
        DnaAnalysisResponseDto mockData = mockDataService.getDnaMockData(reportId);
        return ResponseEntity.ok(ApiResponse.success("DNA JSON 목데이터 조회 완료", mockData));
    }
}
