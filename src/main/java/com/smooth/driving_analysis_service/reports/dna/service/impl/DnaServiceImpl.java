package com.smooth.driving_analysis_service.reports.dna.service.impl;

import com.smooth.driving_analysis_service.global.auth.AuthenticationUtils;
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
            
            // 새로운 String reportId 형식 지원 (u123_c3_interim, u123_c3_final_20250912)
            DnaSnapshot snapshot = dnaSnapshotRepository.findByReportId(reportId).orElse(null);
            
            // 스냅샷이 없으면 레거시 방식으로 시도
            if (snapshot == null && reportId.matches("\\d+")) {
                Long reportIdLong = Long.parseLong(reportId);
                log.info("Trying legacy lookup for numeric reportId: {}", reportIdLong);
                
                snapshot = dnaSnapshotRepository.findByUserIdAndReportIdAndStatus(userId, reportIdLong, DnaSnapshot.Status.FINAL)
                        .orElseGet(() -> dnaSnapshotRepository.findByUserIdAndReportIdAndStatus(userId, reportIdLong, DnaSnapshot.Status.INTERIM)
                                .orElse(null));
            }
            
            if (snapshot == null) {
                log.warn("No DNA snapshot found for reportId: {}, userId: {}", reportId, userId);
                return createDefaultResponse(reportId);
            }
            
            // 사용자 권한 확인
            if (!snapshot.getUserId().equals(userId)) {
                log.warn("User {} does not own DNA snapshot for reportId: {}", userId, reportId);
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
            
            // 메타 정보 생성 (새로운 사이클 기반 시스템)
            DnaAnalysisResponseDto.MetaDto meta = createMetaDto(reportId, snapshot);
            
            return DnaAnalysisResponseDto.builder()
                    .reportId(reportId)
                    .headline(snapshot.getHeadline())
                    .radar(DnaAnalysisResponseDto.RadarDto.builder()
                            .A(snapshot.getSafeDrivingScore().intValue())
                            .B(snapshot.getEcoDrivingScore().intValue())
                            .C(snapshot.getDefensiveDrivingScore().intValue())
                            .D(snapshot.getSmoothDrivingScore().intValue())
                            .build())
                    .axes(List.of(
                            createAxisDto("A", axisMeta.get("A")),
                            createAxisDto("B", axisMeta.get("B")),
                            createAxisDto("C", axisMeta.get("C")),
                            createAxisDto("D", axisMeta.get("D"))
                    ))
                    .meta(meta)
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
     * 새로운 사이클 기반 시스템의 메타 정보 생성
     */
    private DnaAnalysisResponseDto.MetaDto createMetaDto(String reportId, DnaSnapshot snapshot) {
        try {
            boolean isInterim = reportId.contains("_interim");
            String type = isInterim ? "INTERIM" : "FINAL";
            String status = isInterim ? "COLLECTING" : "COMPLETED";
            
            // reportId에서 사이클 번호 추출 (u123_c3_interim -> 3)
            Integer cycleNo = null;
            String[] parts = reportId.split("_");
            for (String part : parts) {
                if (part.startsWith("c") && part.length() > 1) {
                    try {
                        cycleNo = Integer.parseInt(part.substring(1));
                        break;
                    } catch (NumberFormatException e) {
                        log.debug("Could not parse cycle number from: {}", part);
                    }
                }
            }
            
            // INTERIM인 경우 lastInterimCount 사용, FINAL인 경우 15
            Integer sampleSize = isInterim ? snapshot.getLastInterimCount() : 15;
            
            return DnaAnalysisResponseDto.MetaDto.builder()
                    .type(type)
                    .cycleNo(cycleNo)
                    .sampleSize(sampleSize)
                    .status(status)
                    .updatedAt(snapshot.getUpdatedAt())
                    .build();
                    
        } catch (Exception e) {
            log.warn("Error creating meta info for reportId: {}", reportId, e);
            return null; // 메타 정보는 선택적이므로 null 반환
        }
    }
}