package com.parkvision.iam.interfaces.resource;

import java.time.LocalDateTime;

public record UserPreferencesResource(
        Long userId,
        boolean darkMode,
        String language,
        boolean alertFreeSpace,
        boolean alertSaturated,
        boolean alertCameraFailure,
        int alertRadiusM,
        LocalDateTime updatedAt
) {}
