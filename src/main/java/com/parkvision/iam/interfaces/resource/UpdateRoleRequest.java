package com.parkvision.iam.interfaces.resource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateRoleRequest(
        @NotBlank @Size(max = 50) String name,
        @Size(max = 300) String description
) {}
