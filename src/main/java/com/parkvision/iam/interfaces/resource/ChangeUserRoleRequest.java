package com.parkvision.iam.interfaces.resource;

import jakarta.validation.constraints.NotBlank;

public record ChangeUserRoleRequest(@NotBlank String roleName) {}
