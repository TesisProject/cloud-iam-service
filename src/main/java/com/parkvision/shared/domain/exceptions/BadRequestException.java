package com.parkvision.shared.domain.exceptions;

public abstract class BadRequestException extends RuntimeException {

    private final String errorCode;

    protected BadRequestException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
