package com.parkvision.iam.domain.exceptions;

import com.parkvision.shared.domain.exceptions.ConflictException;

public class UserAlreadyExistsException extends ConflictException {

    public UserAlreadyExistsException(String field, String value) {
        super("USER_ALREADY_EXISTS", "User with " + field + " '" + value + "' already exists");
    }
}
