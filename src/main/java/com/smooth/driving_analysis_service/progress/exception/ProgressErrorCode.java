package com.smooth.driving_analysis_service.progress.exception;

import com.smooth.driving_analysis_service.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProgressErrorCode implements ErrorCode {
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, 2001, "리포트를 찾을 수 없습니다."),
    INSUFFICIENT_TRIPS(HttpStatus.BAD_REQUEST, 2002, "리포트 생성 조건(15회 주행)을 만족하지 못했습니다."),
    REPORT_ALREADY_EXISTS(HttpStatus.CONFLICT, 2003, "리포트가 이미 생성되었습니다.");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;
}
