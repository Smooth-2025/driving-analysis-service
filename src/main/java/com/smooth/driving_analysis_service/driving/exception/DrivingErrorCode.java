package com.smooth.driving_analysis_service.driving.exception;

import com.smooth.driving_analysis_service.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum DrivingErrorCode implements ErrorCode {

    DRIVING_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, 5501, "주행기록을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;
}
