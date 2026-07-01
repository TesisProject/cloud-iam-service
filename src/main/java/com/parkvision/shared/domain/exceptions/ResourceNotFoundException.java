package com.parkvision.shared.domain.exceptions;

/**
 * Excepción base para recursos no encontrados. Cada bounded context la extiende con su
 * propio {@code errorCode}, que el {@code GlobalExceptionHandler} mapea a la respuesta HTTP 404.
 */
public abstract class ResourceNotFoundException extends RuntimeException {

    private final String errorCode;

    protected ResourceNotFoundException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
