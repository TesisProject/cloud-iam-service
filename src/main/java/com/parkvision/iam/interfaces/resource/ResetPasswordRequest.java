package com.parkvision.iam.interfaces.resource;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 5, max = 5) String otp,
        @NotBlank @Size(min = 8) String newPassword
) {}
