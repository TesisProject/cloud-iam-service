package com.parkvision.iam.interfaces.resource;

import java.time.LocalDateTime;

public record RoleResource(
        Long id,
        String name,
        String description,
        boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
