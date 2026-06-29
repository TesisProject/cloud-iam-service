package com.parkvision.iam.domain.exceptions;

import com.parkvision.shared.domain.exceptions.UnauthorizedException;

public class InvalidCredentialsException extends UnauthorizedException {

    public InvalidCredentialsException() {
        super("INVALID_CREDENTIALS", "Invalid username or password");
    }
}
