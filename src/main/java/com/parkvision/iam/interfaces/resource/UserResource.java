package com.parkvision.iam.interfaces.resource;

import java.time.LocalDateTime;

public record UserResource(
        Long id,
        String email,
        String role,
        boolean isActive,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
