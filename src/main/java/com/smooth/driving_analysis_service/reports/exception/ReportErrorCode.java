package com.smooth.driving_analysis_service.reports.exception;

import com.smooth.driving_analysis_service.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter

public enum ReportErrorCode implements ErrorCode {
    MOCK_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, 5001, "목데이터을 찾을 수 없습니다."),
    MOCK_DATA_LOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 5002, "목데이터 로딩에 실패했습니다.");

        private final HttpStatus httpStatus;
        private final Integer code;
        private final String message;

    ReportErrorCode(HttpStatus httpStatus, Integer code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

}
