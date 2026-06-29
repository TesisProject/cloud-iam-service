package com.parkvision.iam.domain.exceptions;

import com.parkvision.shared.domain.exceptions.BadRequestException;

public class InvalidOtpException extends BadRequestException {

    public InvalidOtpException() {
        super("INVALID_OTP", "OTP code is invalid or has expired");
    }
}
