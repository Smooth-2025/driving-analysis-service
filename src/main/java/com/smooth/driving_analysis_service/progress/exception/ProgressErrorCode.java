package com.smooth.driving_analysis_service.progress.exception;

import com.smooth.driving_analysis_service.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProgressErrorCode implements ErrorCode {
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, 3001, "리포트를 찾을 수 없습니다."),
    INSUFFICIENT_TRIPS(HttpStatus.BAD_REQUEST, 3002, "리포트 생성 조건(15회 주행)을 만족하지 못했습니다."),
    REPORT_ALREADY_EXISTS(HttpStatus.CONFLICT, 3003, "리포트가 이미 생성되었습니다."),
    FORBIDDEN_USER(HttpStatus.FORBIDDEN, 3004, "해당 사용자 리포트에 접근할 수 없습니다."),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, 3005, "잘못된 요청 파라미터입니다."),
    PROGRESS_CALCULATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 3006, "진행도 계산 중 오류가 발생했습니다."),
    DATA_INCONSISTENCY(HttpStatus.INTERNAL_SERVER_ERROR, 3007, "리포트 데이터가 일관되지 않습니다.");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;
}
