package com.parkvision.iam.domain.exceptions;

import com.parkvision.shared.domain.exceptions.BadRequestException;

public class InvalidPasswordException extends BadRequestException {

    public InvalidPasswordException() {
        super("INVALID_PASSWORD", "Current password is incorrect");
    }
}
