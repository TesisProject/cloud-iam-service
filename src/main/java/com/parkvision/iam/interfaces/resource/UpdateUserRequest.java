package com.parkvision.iam.interfaces.resource;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Email String email,
        @Size(min = 8) String newPassword
) {}
