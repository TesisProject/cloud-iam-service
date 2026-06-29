package com.parkvision.iam.interfaces.resource;

import jakarta.validation.constraints.NotNull;

public record ToggleUserStatusRequest(@NotNull Boolean active) {}
