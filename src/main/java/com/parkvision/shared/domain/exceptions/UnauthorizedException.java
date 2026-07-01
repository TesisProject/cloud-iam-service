package com.parkvision.shared.domain.exceptions;

public abstract class UnauthorizedException extends RuntimeException {

    private final String errorCode;

    protected UnauthorizedException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
