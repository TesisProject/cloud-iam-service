package com.parkvision.iam.interfaces.resource;

import jakarta.validation.constraints.NotNull;

public record AssignPermissionRequest(@NotNull Long permissionId) {}
