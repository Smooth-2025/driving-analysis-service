package com.smooth.driving_analysis_service.reports.dna.service.impl;

import com.smooth.driving_analysis_service.global.util.AuthenticationUtils;
import com.smooth.driving_analysis_service.reports.dna.dto.response.DnaAnalysisResponseDto;
import com.smooth.driving_analysis_service.reports.dna.entity.DnaSnapshot;
import com.smooth.driving_analysis_service.reports.dna.repository.DnaSnapshotRepository;
import com.smooth.driving_analysis_service.reports.dna.service.DnaComputeService;
import com.smooth.driving_analysis_service.reports.dna.service.DnaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DnaServiceImpl implements DnaService {

    private final DnaSnapshotRepository dnaSnapshotRepository;
    private final DnaComputeService dnaComputeService;

    @Override
    public DnaAnalysisResponseDto getDnaAnalysis(String reportId) {
        log.info("Getting DNA analysis for reportId: {}", reportId);
        
        try {
            Long userId = AuthenticationUtils.getCurrentUserIdOrThrow();
            // reportId에서 숫자 부분 추출 (u1_r3_20250901 -> 3)
            Long reportIdLong = extractReportIdNumber(reportId);
            
            log.info("DNA analysis - requested: {}, effectiveReportIdUsed: {}, userId: {}", reportId, reportIdLong, userId);
            
            // DNA 스냅샷 조회 (사용자별, FINAL 우선, 없으면 INTERIM)
            DnaSnapshot snapshot = dnaSnapshotRepository.findByUserIdAndReportIdAndStatus(userId, reportIdLong, DnaSnapshot.Status.FINAL)
                    .orElseGet(() -> dnaSnapshotRepository.findByUserIdAndReportIdAndStatus(userId, reportIdLong, DnaSnapshot.Status.INTERIM)
                            .orElse(null));
            
            if (snapshot == null) {
                log.warn("No DNA snapshot found - requested: {}, used: {}, userId: {}", reportId, reportIdLong, userId);
                return createDefaultResponse(reportId);
            }
            
            // DNA 코드 파싱 (예: "A2-B1-C3-D2")
            String[] parts = snapshot.getCode().split("-");
            if (parts.length != 4) {
                log.warn("Invalid DNA code format: {}", snapshot.getCode());
                return createDefaultResponse(reportId);
            }
            
            String A = parts[0], B = parts[1], C = parts[2], D = parts[3];
            
            // 축별 메타데이터 생성
            Map<String, String[]> axisMeta = dnaComputeService.axisMeta(A, B, C, D);
            
            return DnaAnalysisResponseDto.builder()
                    .reportId(reportId)
                    .headline(snapshot.getHeadline())
                    .radar(DnaAnalysisResponseDto.RadarDto.builder()
                            .A(snapshot.getScoreA())
                            .B(snapshot.getScoreB())
                            .C(snapshot.getScoreC())
                            .D(snapshot.getScoreD())
                            .build())
                    .axes(List.of(
                            createAxisDto("A", axisMeta.get("A")),
                            createAxisDto("B", axisMeta.get("B")),
                            createAxisDto("C", axisMeta.get("C")),
                            createAxisDto("D", axisMeta.get("D"))
                    ))
                    .build();
                    
        } catch (Exception e) {
            log.error("Error getting DNA analysis for reportId: {}", reportId, e);
            return createDefaultResponse(reportId);
        }
    }

    private DnaAnalysisResponseDto.AxisDto createAxisDto(String id, String[] meta) {
        if (meta == null || meta.length < 2) {
            return DnaAnalysisResponseDto.AxisDto.builder()
                    .id(id)
                    .label(id + "2")
                    .summary("분석 중입니다.")
                    .build();
        }
        
        return DnaAnalysisResponseDto.AxisDto.builder()
                .id(id)
                .label(meta[0])
                .summary(meta[1])
                .build();
    }

    private DnaAnalysisResponseDto createDefaultResponse(String reportId) {
        return DnaAnalysisResponseDto.builder()
                .reportId(reportId)
                .headline("무리하지 않는 차분한 주행 스타일이에요!")
                .radar(DnaAnalysisResponseDto.RadarDto.builder()
                        .A(65).B(65).C(65).D(65)
                        .build())
                .axes(List.of(
                        DnaAnalysisResponseDto.AxisDto.builder()
                                .id("A").label("A2").summary("일반형 출발: 무리하지 않는 적절한 가속이에요.")
                                .build(),
                        DnaAnalysisResponseDto.AxisDto.builder()
                                .id("B").label("B2").summary("반응 감속형: 상황에 맞춰 적절히 감속해요.")
                                .build(),
                        DnaAnalysisResponseDto.AxisDto.builder()
                                .id("C").label("C2").summary("중립형: 필요 시 적절히 차선을 변경해요.")
                                .build(),
                        DnaAnalysisResponseDto.AxisDto.builder()
                                .id("D").label("D2").summary("상황 유지형: 큰 무리 없이 안정적으로 대응해요.")
                                .build()
                ))
                .build();
    }

    /**
     * reportId에서 숫자 부분 추출 (u1_r3_20250901 -> 3, 또는 단순 숫자 "13" -> 13)
     */
    private Long extractReportIdNumber(String reportId) {
        if (reportId == null || reportId.trim().isEmpty()) {
            log.warn("Empty reportId provided, using default 1L");
            return 1L;
        }
        
        try {
            // 1. 단순 숫자인 경우 직접 파싱
            if (reportId.matches("\\d+")) {
                Long result = Long.parseLong(reportId);
                log.debug("Parsed simple numeric reportId: {} -> {}", reportId, result);
                return result;
            }
            
            // 2. u1_r3_20250901 형식에서 r 다음 숫자 추출
            String[] parts = reportId.split("_");
            for (String part : parts) {
                if (part.startsWith("r") && part.length() > 1) {
                    String numberPart = part.substring(1);
                    if (numberPart.matches("\\d+")) {
                        Long result = Long.parseLong(numberPart);
                        log.debug("Extracted reportId from formatted string: {} -> {}", reportId, result);
                        return result;
                    }
                }
            }
            
            // 3. 패턴이 맞지 않으면 경고 후 기본값 사용
            log.warn("Cannot extract reportId number from: '{}', using default 1L", reportId);
            return 1L;
        } catch (NumberFormatException e) {
            log.warn("Error parsing reportId number from: '{}', using default 1L - {}", reportId, e.getMessage());
            return 1L;
        } catch (Exception e) {
            log.warn("Unexpected error parsing reportId: '{}', using default 1L", reportId, e);
            return 1L;
        }
    }
}