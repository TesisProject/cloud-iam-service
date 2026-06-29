package com.parkvision.iam.domain.exceptions;

import com.parkvision.shared.domain.exceptions.UnauthorizedException;

public class InvalidApiKeyException extends UnauthorizedException {

    public InvalidApiKeyException() {
        super("INVALID_API_KEY", "Invalid or revoked API key");
    }
}
