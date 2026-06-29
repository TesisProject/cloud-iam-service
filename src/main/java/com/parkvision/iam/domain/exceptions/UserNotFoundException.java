package com.parkvision.iam.domain.exceptions;

import com.parkvision.shared.domain.exceptions.ResourceNotFoundException;

public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(Long userId) {
        super("USER_NOT_FOUND", "User not found: " + userId);
    }

    public UserNotFoundException(String email) {
        super("USER_NOT_FOUND", "User not found: " + email);
    }
}
