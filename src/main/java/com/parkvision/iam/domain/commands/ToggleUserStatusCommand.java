package com.parkvision.iam.domain.commands;

public record ToggleUserStatusCommand(Long userId, boolean active) {}
