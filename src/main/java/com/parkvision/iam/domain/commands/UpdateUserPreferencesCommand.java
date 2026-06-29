package com.parkvision.iam.domain.commands;

public record UpdateUserPreferencesCommand(
        Long userId,
        boolean darkMode,
        String language,
        boolean alertFreeSpace,
        boolean alertSaturated,
        boolean alertCameraFailure,
        int alertRadiusM
) {}
