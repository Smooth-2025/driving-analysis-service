package com.smooth.driving_analysis_service.driving.exception;

import com.smooth.driving_analysis_service.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum DrivingErrorCode implements ErrorCode {

    DRIVING_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, 5501, "주행기록을 찾을 수 없습니다."),
    DRIVING_RECORD_NOT_FOUND_IN_PERIOD(HttpStatus.NOT_FOUND, 5502, "해당 기간에 주행기록을 찾을 수 없습니다."),

    CHARACTER_NOT_FOUND(HttpStatus.NOT_FOUND, 5601, "캐릭터 성향을 찾을 수 없습니다."),
    DRIVING_STATE_NOT_FOUND(HttpStatus.NOT_FOUND, 5602, "주행 상태를 찾을 수 없습니다."),
    CHARACTER_CANT_START(HttpStatus.BAD_REQUEST, 5603, "캐릭터 성향 분석을 시작할 수 없습니다."),

    REDIS_STREAM_PUBLISH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 5701, "이벤트 발행에 실패했습니다."),

    AI_MODEL_INVOCATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 5801, "AI 모델 호출에 실패했습니다."),
    AI_MODEL_RESPONSE_PARSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 5802, "AI 모델 응답 파싱에 실패했습니다."),
    AI_MODEL_UNEXPECTED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 5803, "AI 모델 처리 중 예기치 못한 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;
}
