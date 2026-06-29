package com.parkvision.iam.interfaces.resource;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserPreferencesRequest(
        boolean darkMode,
        @NotBlank @Size(max = 10) String language,
        boolean alertFreeSpace,
        boolean alertSaturated,
        boolean alertCameraFailure,
        @Min(0) int alertRadiusM
) {}
