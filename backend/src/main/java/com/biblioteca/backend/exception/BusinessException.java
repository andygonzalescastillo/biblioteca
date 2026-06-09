package com.biblioteca.backend.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;

    private BusinessException(String errorCode, String message, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public static BusinessException badRequest(String errorCode, String message) {
        return new BusinessException(errorCode, message, HttpStatus.BAD_REQUEST);
    }

    public static BusinessException conflict(String errorCode, String message) {
        return new BusinessException(errorCode, message, HttpStatus.CONFLICT);
    }

    public String errorCode() {
        return errorCode;
    }

    public HttpStatus status() {
        return status;
    }
}
