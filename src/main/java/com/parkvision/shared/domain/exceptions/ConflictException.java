package com.parkvision.shared.domain.exceptions;

public abstract class ConflictException extends RuntimeException {

    private final String errorCode;

    protected ConflictException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
