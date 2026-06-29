package com.parkvision.iam.domain.exceptions;

import com.parkvision.shared.domain.exceptions.ResourceNotFoundException;

public class ApiKeyNotFoundException extends ResourceNotFoundException {

    public ApiKeyNotFoundException(Long apiKeyId) {
        super("API_KEY_NOT_FOUND", "API key not found: " + apiKeyId);
    }
}
