package com.parkvision.iam.interfaces.resource;

import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        @Size(max = 20) String phone,
        @Size(max = 500) String avatarUrl,
        String bio
) {}
