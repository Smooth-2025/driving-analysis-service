package com.smooth.driving_analysis_service.reports.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.AccidentReactionReportResponseDto;
import com.smooth.driving_analysis_service.reports.basic_summary.dto.BasicSummaryResponseDto;
import com.smooth.driving_analysis_service.reports.behavior.dto.response.BehaviorAnalysisResponseDto;
import com.smooth.driving_analysis_service.reports.dna.dto.response.DnaAnalysisResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MockDataService {

    private final ObjectMapper objectMapper;

    /**
     * DNA 목데이터를 DTO로 변환해서 반환
     */
    public DnaAnalysisResponseDto getDnaMockData(String reportId) {
        try {
            JsonNode jsonNode = readMockDataWithReportId("dna.json", reportId);
            JsonNode dataNode = jsonNode.get("data");
            return objectMapper.treeToValue(dataNode, DnaAnalysisResponseDto.class);
        } catch (Exception e) {
            log.error("Failed to load DNA mock data", e);
            throw new RuntimeException("Failed to load DNA mock data", e);
        }
    }

    /**
     * 사고 반응 목데이터를 DTO로 변환해서 반환
     */
    public AccidentReactionReportResponseDto getAccidentReactionMockData(String reportId) {
        try {
            JsonNode jsonNode = readMockDataWithReportId("accident-reaction.json", reportId);
            JsonNode dataNode = jsonNode.get("data");
            return objectMapper.treeToValue(dataNode, AccidentReactionReportResponseDto.class);
        } catch (Exception e) {
            log.error("Failed to load accident reaction mock data", e);
            throw new RuntimeException("Failed to load accident reaction mock data", e);
        }
    }

    /**
     * 기본 요약 목데이터를 DTO로 변환해서 반환
     */
    public BasicSummaryResponseDto getBasicSummaryMockData(String reportId) {
        try {
            JsonNode jsonNode = readMockDataWithReportId("basic-summary.json", reportId);
            JsonNode dataNode = jsonNode.get("data");
            return objectMapper.treeToValue(dataNode, BasicSummaryResponseDto.class);
        } catch (Exception e) {
            log.error("Failed to load basic summary mock data", e);
            throw new RuntimeException("Failed to load basic summary mock data", e);
        }
    }

    /**
     * 위험운전 행동 목데이터를 DTO로 변환해서 반환
     */
    public BehaviorAnalysisResponseDto getBehaviorMockData(String reportId) {
        try {
            JsonNode jsonNode = readMockDataWithReportId("behavior.json", reportId);
            JsonNode dataNode = jsonNode.get("data");
            return objectMapper.treeToValue(dataNode, BehaviorAnalysisResponseDto.class);
        } catch (Exception e) {
            log.error("Failed to load behavior mock data", e);
            throw new RuntimeException("Failed to load behavior mock data", e);
        }
    }

    /**
     * resources/mock 디렉토리에서 JSON 파일을 읽어서 반환
     */
    private JsonNode readMockData(String fileName) {
        try {
            ClassPathResource resource = new ClassPathResource("mock/" + fileName);
            try (InputStream inputStream = resource.getInputStream()) {
                JsonNode jsonNode = objectMapper.readTree(inputStream);
                log.info("Mock data loaded successfully: {}", fileName);
                return jsonNode;
            }
        } catch (IOException e) {
            log.error("Failed to read mock data file: {}", fileName, e);
            throw new RuntimeException("Mock data file not found: " + fileName, e);
        }
    }

    /**
     * Mock 데이터의 reportId를 동적으로 변경
     */
    private JsonNode readMockDataWithReportId(String fileName, String reportId) {
        try {
            JsonNode jsonNode = readMockData(fileName);
            // reportId 변경
            if (jsonNode.has("data") && jsonNode.get("data").has("reportId")) {
                ((com.fasterxml.jackson.databind.node.ObjectNode) jsonNode.get("data"))
                    .put("reportId", reportId);
            }
            return jsonNode;
        } catch (Exception e) {
            log.error("Failed to update reportId in mock data: {}", fileName, e);
            throw new RuntimeException("Failed to process mock data: " + fileName, e);
        }
    }
}