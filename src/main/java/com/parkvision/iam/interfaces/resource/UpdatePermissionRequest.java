package com.parkvision.iam.interfaces.resource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePermissionRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 100) String resource,
        @NotBlank @Size(max = 100) String action,
        @Size(max = 300) String description
) {}
