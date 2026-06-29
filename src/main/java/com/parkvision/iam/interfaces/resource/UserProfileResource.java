package com.parkvision.iam.interfaces.resource;

import java.time.LocalDateTime;

public record UserProfileResource(
        Long userId,
        String firstName,
        String lastName,
        String phone,
        boolean isActive,
        String avatarUrl,
        String bio,
        LocalDateTime updatedAt
) {}
