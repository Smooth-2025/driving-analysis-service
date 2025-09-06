package com.smooth.driving_analysis_service.driving.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smooth.driving_analysis_service.driving.dto.request.DrivingCharacterAnalysisRequestDto;
import com.smooth.driving_analysis_service.driving.dto.result.DrivingCharacterAnalysisResultDto;
import com.smooth.driving_analysis_service.driving.exception.DrivingErrorCode;
import com.smooth.driving_analysis_service.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

@Slf4j
@RequiredArgsConstructor
@Service
public class BedrockServiceImpl implements BedrockService {

    private final BedrockRuntimeClient bedrockRuntimeClient;
    private final ObjectMapper objectMapper;

    public DrivingCharacterAnalysisResultDto invokeModel(DrivingCharacterAnalysisRequestDto requestDto) {

        var modelId = "apac.anthropic.claude-3-7-sonnet-20250219-v1:0";

        var nativeRequestTemplate = """
                {
                    "anthropic_version": "bedrock-2023-05-31",
                    "max_tokens": 1000,
                    "temperature": 0.5,
                    "messages": [{
                                    "role": "user",
                                    "content": [{
                                        "type": "text",
                                        "text": "{{prompt}}"
                                    }]
                                }]
                }""";

        var prompt = String.format("""
                        다음 운전자의 100km 주행 데이터를 분석하여 4가지 동물 타입 중 하나로 분류해주세요.
                        
                        === 100km 주행 데이터 ===
                        기본 정보:
                        - 총 주행거리: %.1fkm
                        - 주행시간: %.1f분
                        - 평균속도: %.1fkm/h
                        - 최고속도: %.1fkm/h
                        - 정속률: %.1f
                        
                        100km당 주행 패턴:
                        - 차선변경: %.1f회/100km (실제 %d회)
                        - 급가속: %.1f회/100km (실제 %d회)
                        - 급제동: %.1f회/100km (실제 %d회)  
                        - 급회전: %.1f회/100km (실제 %d회)
                        - 총 급조작: %.1f회/100km
                        
                        === 동물 타입 분류 기준 (100km 기준) ===
                        
                        **CAT 타입**: 안전하고 신중함, 방어적 운전자, 꾸준한 운전자, 규칙 준수자
                        - 정속률 70 이상
                        - 100km당 급조작 8회 이하
                        - 100km당 차선변경 12회 이하
                        - 평균속도 적정 (60-90km/h) 
                        - 안정적이고 예측 가능한 주행 패턴
                        
                        **DOLPHIN 타입**: 부드러운 운전자, 효율적 크루저, 친환경 운전자, 흐름 마스터
                        - 정속률 50-70
                        - 100km당 급조작 8-15회
                        - 100km당 차선변경 12-20회
                        - 교통 흐름에 맞춘 속도 (70-100km/h)
                        - 부드럽고 효율적인 주행 스타일
                        
                        **LION 타입**: 속도광, 공격적 운전자, 위험 감수자, 고속도로 전사
                        - 고속 주행 (평균속도 90km/h 이상)
                        - 100km당 급조작 15회 이상
                        - 100km당 차선변경 20회 이상
                        - 최고속도 높음 (120km/h 이상)
                        - 공격적이고 역동적인 주행 패턴
                        
                        **MEERKAT 타입**: 조심스러운 초보, 경계심 많은 운전자, 긴장하는 운전자, 안전 제일주의자
                        - 저속 주행 (평균속도 60km/h 이하) 또는 과도한 조심
                        - 정속률 낮음 (50 이하) 또는 매우 높음 (90 이상)
                        - 100km당 급조작이 많을 수 있음 (미숙함으로 인한)
                        - 예측하기 어려운 주행 패턴
                        - 과도하게 신중하거나 긴장된 운전
                        
                        === 응답 형식 ===
                        다음 JSON 형식으로만 정확히 응답해주세요: ```json ```이 아닌 text 형식으로
                        
                        {
                            "characterType": "CAT/DOLPHIN/LION/MEERKAT 중 하나",
                            "confidenceScore": 85,
                            "characterTrait": "특성(타입 이름 옆의 특성 중 하나)",
                            "drivingStyle": "구체적인 운전 스타일 설명",
                            "speedPreference": "속도 선호도 분석", 
                            "improvementSuggestions": "개선 조언",
                            "personalityDescription": "100km 주행 패턴을 기반으로 한 운전자 특성의 상세한 설명(당신은.. 이렇게 시작)"
                        }
                        """,

                requestDto.getTotalDistanceKm(),
                requestDto.getDrivingMinutes(),
                requestDto.getAvgSpeed(),
                requestDto.getMaxSpeed(),
                requestDto.getCruiseRatio(),
                requestDto.getLaneChangeCount() * 100.0 / requestDto.getTotalDistanceKm(), requestDto.getLaneChangeCount(),
                requestDto.getRapidAccelCount() * 100.0 / requestDto.getTotalDistanceKm(), requestDto.getRapidAccelCount(),
                requestDto.getHardBrakeCount() * 100.0 / requestDto.getTotalDistanceKm(), requestDto.getHardBrakeCount(),
                requestDto.getSharpTurnCount() * 100.0 / requestDto.getTotalDistanceKm(), requestDto.getSharpTurnCount(),
                requestDto.getTotalAggressiveCount() * 100.0 / requestDto.getTotalDistanceKm()


        );

        String escapedPrompt = prompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");

        String nativeRequest = nativeRequestTemplate.replace("{{prompt}}", escapedPrompt);

        try {

            var response = bedrockRuntimeClient.invokeModel(request -> request
                    .body(SdkBytes.fromUtf8String(nativeRequest))
                    .modelId(modelId)
            );

            JsonNode responseBody = objectMapper.readTree(response.body().asUtf8String());

            String text = responseBody.at("/content/0/text").asText();

            DrivingCharacterAnalysisResultDto resultDto = objectMapper.readValue(text, DrivingCharacterAnalysisResultDto.class);

            return resultDto;

        } catch (SdkClientException e) {
            log.error("ERROR: Can't invoke '{}'. Reason: {}", modelId, e.getMessage(), e);
            throw new BusinessException(DrivingErrorCode.AI_MODEL_INVOCATION_FAILED);

        } catch (JsonProcessingException e) {
            throw new BusinessException(DrivingErrorCode.AI_MODEL_RESPONSE_PARSING_FAILED);
        } catch (RuntimeException e) {
            log.error("ERROR: Can't invoke '{}'. Reason: {}", modelId, e.getMessage(), e);
            throw new BusinessException(DrivingErrorCode.AI_MODEL_UNEXPECTED_ERROR);
        }
    }
}
